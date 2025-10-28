package com.wildermods.multimyth.internal;

import java.nio.file.Path;

import com.wildermods.thrixlvault.steam.IDownloadable;

public record Install<T extends IDownloadable>(T game, Path installPath, JVMBinary java, boolean isCoremodInstall) {


}
