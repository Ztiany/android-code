package com.geektime.asm.transform

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformTask
import com.geektime.asm.MethodTracer
import com.geektime.asm.Util
import com.geektime.asm.Log

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.execution.TaskExecutionGraphListener

import java.lang.reflect.Field


public class ASMTraceTransform extends BaseProxyTransform {

    Transform origTransform
    Project project
    def variant

    ASMTraceTransform(Project project, def variant, Transform origTransform) {
        super(origTransform)
        this.origTransform = origTransform
        this.variant = variant
        this.project = project
    }

    public static void inject(Project project, def variant) {

        String hackTransformTaskName = getTransformTaskName(
                "",
                "", variant.name
        )

        String hackTransformTaskNameForWrapper = getTransformTaskName(
                "",
                "Builder", variant.name
        )

        project.logger.info("prepare inject dex transform :" + hackTransformTaskName + " hackTransformTaskNameForWrapper:" + hackTransformTaskNameForWrapper)

        project.getGradle().getTaskGraph().addTaskExecutionGraphListener(new TaskExecutionGraphListener() {
            @Override
            public void graphPopulated(TaskExecutionGraph taskGraph) {
                for (Task task : taskGraph.getAllTasks()) {
                    if ((task.name.equalsIgnoreCase(hackTransformTaskName) || task.name.equalsIgnoreCase(hackTransformTaskNameForWrapper))
                            && !(((TransformTask) task).getTransform() instanceof ASMTraceTransform)) {

                        project.logger.warn("find dex transform. transform class: " + task.transform.getClass() + " . task name: " + task.name)
                        project.logger.info("variant name: " + variant.name)
                        Field field = TransformTask.class.getDeclaredField("transform")
                        field.setAccessible(true)
                        field.set(task, new ASMTraceTransform(project, variant, task.transform))
                        project.logger.warn("transform class after hook: " + task.transform.getClass())
                        break

                    }
                }
            }
        })
    }

    @Override
    public String getName() {
        return "ASMTraceTransform"
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        long start = System.currentTimeMillis()
        final boolean isIncremental = transformInvocation.isIncremental() && this.isIncremental()
        final File rootOutput = new File(project.getBuildDir().getAbsolutePath() + File.separator + "asmoutput", "classes/${getName()}/")

        //Chapter27-ASM\ASMSample\build\asmoutput\classes\ASMTraceTransform
        Log.i("ASM." + getName(), " rootOutput = $rootOutput")

        if (!rootOutput.exists()) {
            rootOutput.mkdirs()
        }

        /*????????? map ?????????????????????jar???????????????*/
        Map<File, File> jarInputMap = new HashMap<>()
        Map<File, File> scrInputMap = new HashMap<>()

        transformInvocation.inputs.each { TransformInput input ->
            //????????????
            input.directoryInputs.each { DirectoryInput dirInput ->
                collectAndIdentifyDir(scrInputMap, dirInput, rootOutput, isIncremental)
            }
            //??????jar
            input.jarInputs.each { JarInput jarInput ->
                if (jarInput.getStatus() != Status.REMOVED) {
                    collectAndIdentifyJar(jarInputMap, scrInputMap, jarInput, rootOutput, isIncremental)
                }
            }
        }

        //???????????????
        MethodTracer methodTracer = new MethodTracer()
        methodTracer.trace(scrInputMap, jarInputMap)

        //???????????????
        origTransform.transform(transformInvocation)
        Log.i("ASM." + getName(), "[transform] cost time: %dms", System.currentTimeMillis() - start)
    }

    private void collectAndIdentifyDir(Map<File, File> dirInputMap, DirectoryInput input, File rootOutput, boolean isIncremental) {
        final File dirInput = input.file
        final File dirOutput = new File(rootOutput, input.file.getName())
        if (!dirOutput.exists()) {
            dirOutput.mkdirs()
        }

        if (isIncremental) {
            if (!dirInput.exists()) {
                dirOutput.deleteDir()
            } else {

                final Map<File, Status> obfuscatedChangedFiles = new HashMap<>()
                final String rootInputFullPath = dirInput.getAbsolutePath()
                final String rootOutputFullPath = dirOutput.getAbsolutePath()

                input.changedFiles.each { Map.Entry<File, Status> entry ->
                    //????????????
                    final File changedFileInput = entry.getKey()
                    final String changedFileInputFullPath = changedFileInput.getAbsolutePath()
                    //???????????????
                    final File changedFileOutput = new File(changedFileInputFullPath.replace(rootInputFullPath, rootOutputFullPath))

                    final Status status = entry.getValue()
                    switch (status) {
                        case Status.NOTCHANGED:
                            break
                        case Status.ADDED:
                        case Status.CHANGED://???????????????????????????
                            dirInputMap.put(changedFileInput, changedFileOutput)
                            break
                        case Status.REMOVED://input ???????????????????????? out ????????????
                            changedFileOutput.delete()
                            break
                    }
                    obfuscatedChangedFiles.put(changedFileOutput, status)
                }
                replaceChangedFile(input, obfuscatedChangedFiles)
            }

        } else {
            dirInputMap.put(dirInput, dirOutput)
        }
        replaceFile(input, dirOutput)
    }

    private void collectAndIdentifyJar(Map<File, File> jarInputMaps, Map<File, File> dirInputMaps, JarInput input, File rootOutput, boolean isIncremental) {
        final File jarInput = input.file
        final File jarOutput = new File(rootOutput, getUniqueJarName(jarInput))

        if (Util.isRealZipOrJar(jarInput)) {
            switch (input.status) {
                case Status.NOTCHANGED:
                    if (isIncremental) {
                        break
                    }
                case Status.ADDED:
                case Status.CHANGED:
                    jarInputMaps.put(jarInput, jarOutput)
                    break
                case Status.REMOVED:
                    break
            }
        } else {
            processWeChatAutoDex(jarInput, jarOutput, jarInputMaps, dirInputMaps, rootOutput)
        }

        replaceFile(input, jarOutput)
    }

    static private String getTransformTaskName(String customDexTransformName, String wrappSuffix, String buildTypeSuffix) {
        if (customDexTransformName != null && customDexTransformName.length() > 0) {
            return customDexTransformName + "For${buildTypeSuffix}"
        }
        return "transformClassesWithDex${wrappSuffix}For${buildTypeSuffix}"
    }

    private static void processWeChatAutoDex(File jarInput, File jarOutput, Map<File, File> jarInputMaps, Map<File, File> dirInputMaps, File rootOutput) {
        // Special case for WeChat AutoDex. Its rootInput jar file is actually
        // a txt file contains path list.
        BufferedReader br = null
        BufferedWriter bw = null
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(jarInput)))
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(jarOutput)))

            String realJarInputFullPath
            while ((realJarInputFullPath = br.readLine()) != null) {
                // src jar.
                final File realJarInput = new File(realJarInputFullPath)
                // dest jar, moved to extraguard intermediate output dir.
                final File realJarOutput = new File(rootOutput, getUniqueJarName(realJarInput))

                if (realJarInput.exists() && Util.isRealZipOrJar(realJarInput)) {
                    jarInputMaps.put(realJarInput, realJarOutput)
                } else {
                    realJarOutput.delete()
                    if (realJarInput.exists() && realJarInput.isDirectory()) {
                        realJarOutput = new File(rootOutput, realJarInput.getName())
                        if (!realJarOutput.exists()) {
                            realJarOutput.mkdirs()
                        }
                        dirInputMaps.put(realJarInput, realJarOutput)
                    }

                }
                // write real output full path to the fake jar at rootOutput.
                final String realJarOutputFullPath = realJarOutput.getAbsolutePath()
                bw.writeLine(realJarOutputFullPath)
            }

        } catch (FileNotFoundException e) {
            Log.e("ASM." + getName(), "FileNotFoundException:%s", e.toString())
        } finally {
            Util.closeQuietly(br)
            Util.closeQuietly(bw)
        }
        jarInput.delete() // delete raw inputList
    }

}
