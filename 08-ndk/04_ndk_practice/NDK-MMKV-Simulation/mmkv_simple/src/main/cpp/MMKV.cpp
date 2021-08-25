//
// Created by Administrator on 2019/11/6.
//


#include "MMKV.h"
#include "MMKVLog.h"
#include "PBUtility.h"
#include <string>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/mman.h>


static unordered_map<string, MMKV *> *g_instanceDic = 0;
static string g_rootDir;

//默认的mmkv文件
#define DEFAULT_MMAP_ID "mmkv.default"

//系统给我们提供真正的内存时，用页为单位提供
//内存分页大小 一分页的大小
const int DEFAULT_MMAP_SIZE = getpagesize();

const int32_t Fixed32Size = 4;

void MMKV::initializeMMKV(const char *path) {
    //初始化一个 map
    g_instanceDic = new unordered_map<string, MMKV *>;
    //初始化根目录
    g_rootDir = path;
    //创建目录，后面 777 就是 Linux 常规权限，前面的 0 比较复杂，与用户id和进程都有关系。
    mkdir(g_rootDir.c_str(), 0777);
}

MMKV *MMKV::defaultMMKV() {
    return mmkvWithID(DEFAULT_MMAP_ID);
}

MMKV *MMKV::mmkvWithID(const string &mmapID) {
    //先从map里面找 mmapID 对应的 MMKV 对象
    auto itr = g_instanceDic->find(mmapID);
    if (itr != g_instanceDic->end()) {
        MMKV *kv = itr->second;
        return kv;
    }
    //没有找到，则创建并放入集合。
    auto kv = new MMKV(mmapID);
    (*g_instanceDic)[mmapID] = kv;
    return kv;
}

MMKV::MMKV(const string &mmapID) : m_mmapID(mmapID), m_path(g_rootDir + "/" + mmapID) {
    //从文件里加载已经存储的 key-value
    loadFromFile();
}

MMKV::~MMKV() {
    delete m_output;
    m_output = 0;
    munmap(m_ptr, m_size);
    m_ptr = 0;
    ::close(m_fd);
    auto iter = m_dic.begin();
    while (iter != m_dic.end()) {
        delete iter->second;
        iter = m_dic.erase(iter);
    }
}

//todo 不再使用，请调用close方法(自己编写jni方法，通过jni调用)
void MMKV::close() {
    auto itr = g_instanceDic->find(m_mmapID);
    if (itr != g_instanceDic->end()) {
        g_instanceDic->erase(itr);
    }
    delete this;
}

void MMKV::loadFromFile() {
    /*
     * O_RDWR | O_CREAT 文件操作参数：创建并可读写
     * S_IRWXU 为权限参数：read, write, execute/search by owner
     */
    m_fd = open(m_path.c_str(), O_RDWR | O_CREAT, S_IRWXU);

    if (m_fd < 0) {
        //打開失敗
        LOGI("打开文件:%s 失败！", m_path.c_str());
    }
    //读取文件大小
    struct stat st = {0};
    if (fstat(m_fd, &st) != -1) {
        m_size = st.st_size;
    }
    LOGI("打开文件:%s [%d]", m_path.c_str(), m_size);

    /**
     * 健壮性：文件是否已存在，容量是否满足页大小倍数。
     *
     * 这里省略了各种健壮性检测，包括以下内容：
     *      - 文件是否已存在
     *      - 容量是否满足页大小倍数（至于为什么要满足页的整数倍，还有待进一步研究）
     */

    //调用 mmap 函数进行内存文件映射。
    m_ptr = static_cast<int8_t *>(mmap(0, m_size, PROT_READ | PROT_WRITE, MAP_SHARED, m_fd, 0));

    //规定：文件头4个字节写了数据的有效长度
    memcpy(&m_actualSize, m_ptr, Fixed32Size);

    bool loadFromFile = false;

    //有数据才去读，但是要校验文件的长度。
    if (m_actualSize > 0) {
        //数据长度有效：不能比文件还大
        if (m_actualSize + Fixed32Size <= m_size) {
            loadFromFile = true;
        }
        //其他情况，MMKV是交给用户选择
        // 1、OnErrorDiscard 忽略错误，MMKV会忽略文件中原来的内容
        // 2、OnErrorRecover 还原，MMKV尝试按照自己的方式解析文件，并修正长度
    }

    /**
     * 真正地去解析 mmkv 文件中的数据 保存到 map集合中
     */
    if (loadFromFile) {
        // 封装的protobuf解析器
        InputBuffer inputBuffer(m_ptr + Fixed32Size/*加4个字节，从第5个字节开始解析*/, m_actualSize);

        while (!inputBuffer.isAtEnd()) {
            //读 key
            string key = inputBuffer.readString();

            if (key.length() > 0) {
                //读取value（包含value长度+value数据）
                //这里 InputBuffer 其实只保持了 value 所在位置与长度，并没有正在去读出来，因为真正 get 的时候，才知道要解析为的类型。
                InputBuffer *value = inputBuffer.readData();

                // 检测是否有重复的数据，从集合中找到了老数据（这是 MMKV 的追加存储策略造成的）
                auto iter = m_dic.find(key);
                if (iter != m_dic.end()) {
                    //清理老数据
                    delete iter->second;
                    // java-> map.remove
                    m_dic.erase(key);
                }

                //本次数据有效，加入集合
                if (value && value->length() > 0) {
                    // java-> map.insert
                    m_dic.emplace(key, value);
                }
            }
        }
        //创建输出
        m_output = new OutputBuffer(m_ptr + Fixed32Size + m_actualSize,m_size - Fixed32Size - m_actualSize);
    } else {
        //todo 文件有问题，忽略文件已存在的数据
    }
}

int32_t MMKV::getInt(const char *key, int defaultValue) {
    auto itr = m_dic.find(key);
    // 找到了
    if (itr != m_dic.end()) {
        // 获得value-> 解码器
        InputBuffer *buf = itr->second;
        int32_t returnValue = buf->readInt32();
        //多次读取，将position还原为0
        buf->restore();
        return returnValue;
    }
    return defaultValue;
}

void MMKV::putInt(const char *key, int value) {
    //计算编码 value 需要的长度
    size_t size = computeInt32Size(value);
    //编码value
    auto *buffer = new InputBuffer(size);
    OutputBuffer buf(buffer->data(), buffer->length());
    buf.writeInt32(value);

    //记录到内存
    auto itr = m_dic.find(key);
    if (itr != m_dic.end()) {
        delete itr->second;
    }
    m_dic[key] = buffer;

    //同步到映射区(文件)
    appendDataWithKey(key, buffer);
}

void MMKV::appendDataWithKey(string key, InputBuffer *value) {
    //计算保存这个key-value需要多少字节
    size_t itemSize = computeItemSize(key, value);
    // 空闲空间不够了
    if (itemSize > m_output->spaceLeft()) {
        // 计算去重key后的数据 所需的存储空间
        size_t needSize = computeMapSize(m_dic);
        needSize += Fixed32Size; //总长度 4字节
        //小于文件大小
        if (needSize >= m_size) {
            int32_t oldSize = m_size;
            do {
                //扩充一倍  为什么？？？ mmap规则限制：整数倍
                m_size *= 2;
            } while (needSize >= m_size);
            //重新设定文件大小
            ftruncate(m_fd, m_size);
            //解除映射
            munmap(m_ptr, oldSize);
            //重新映射
            m_ptr = (int8_t *) mmap(m_ptr, m_size, PROT_READ | PROT_WRITE, MAP_SHARED, m_fd, 0);
        }
        //全量更新
        writeAcutalSize(needSize - Fixed32Size);
        delete m_output;
        //创建输出
        m_output = new OutputBuffer(m_ptr + Fixed32Size, m_size - Fixed32Size);
        //把map写入文件
        auto iter = m_dic.begin();
        for (; iter != m_dic.end(); iter++) {
            auto k = iter->first;
            auto v = iter->second;
            m_output->writeString(k);
            m_output->writeData(v);
        }

    } else {
        //增量更新
        writeAcutalSize(m_actualSize + itemSize);
        //写入key
        m_output->writeString(key);
        //写入value
        m_output->writeData(value);
    }
}

void MMKV::writeAcutalSize(size_t size) {
    memcpy(m_ptr, &size, Fixed32Size);
    m_actualSize = size;
}




