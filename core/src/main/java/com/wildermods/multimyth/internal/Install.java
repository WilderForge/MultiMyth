package com.wildermods.multimyth.internal;

import java.nio.file.Path;
import com.wildermods.thrixlvault.steam.IDownloadable;
import com.wildermods.thrixlvault.steam.IGame;

public record Install<T extends IDownloadable & IGame>(int schema, T game, @Transient Path installPath, JVMBinary java, boolean isCoremodded) {

	public static final int CURRENT_SCHEMA = 1;
	
	public Install(T game, Path installPath, JVMBinary java, boolean isCoremodded) {
		this(CURRENT_SCHEMA, game, installPath, java, isCoremodded);
	}
	
}
