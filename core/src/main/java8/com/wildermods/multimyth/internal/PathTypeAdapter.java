package com.wildermods.multimyth.internal;

import java.io.IOException;
import java.nio.file.Path;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class PathTypeAdapter extends TypeAdapter<Path>{

	@Override
	public void write(JsonWriter out, Path value) throws IOException {
		out.value(value.toString());
	}

	@Override
	public Path read(JsonReader in) throws IOException {
		return Path.of(in.nextString());
	}

}
