/*
 * Tencent is pleased to support the open source community by making
 * MMKV available.
 *
 * Copyright (C) 2018 THL A29 Limited, a Tencent company.
 * All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *       https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef MMKV_MMAPEDFILE_H
#define MMKV_MMAPEDFILE_H

#include <string>
#include <sys/ioctl.h>
#include <sys/mman.h>


extern const int DEFAULT_MMAP_SIZE;

class MmapedFile {
    std::string m_name;
    int m_fd;
    void *m_segmentPtr;
    size_t m_segmentSize;

    MmapedFile(const MmapedFile &other) = delete;

    MmapedFile &operator=(const MmapedFile &other) = delete;

public:
    MmapedFile(const std::string &path);

    ~MmapedFile();

    size_t getFileSize() { return m_segmentSize; }

    void *getMemory() { return m_segmentPtr; }

    std::string &getName() { return m_name; }

    int getFd() { return m_fd; }

    bool isFileValid() {
        return m_fd >= 0 && m_segmentSize > 0 && m_segmentPtr && m_segmentPtr != MAP_FAILED;
    }
};

extern bool removeFile(const std::string &nsFilePath);

extern bool zeroFillFile(int fd, size_t startPos, size_t size);

#endif //MMKV_MMAPEDFILE_H
