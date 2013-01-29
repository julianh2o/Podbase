package util;

import ij.ImagePlus;
import ij.process.ImageStatistics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import models.DatabaseImage;
import models.GsonTransient;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import controllers.ImageBrowser;

public class PodbaseUtil {

	public static Gson getGsonExcludesGsonTransient() {
		Gson gson = new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {
			@Override
			public boolean shouldSkipClass(Class<?> arg0) {
				return false;
			}

			@Override
			public boolean shouldSkipField(FieldAttributes field) {
				GsonTransient annotation = field.getAnnotation(GsonTransient.class);
				return annotation != null;
			}}).create();
		return gson;
	}

}
