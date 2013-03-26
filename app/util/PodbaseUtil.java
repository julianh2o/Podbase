package util;

import ij.ImagePlus;
import ij.process.ImageStatistics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;

import models.DatabaseImage;
import models.GsonTransient;
import models.ImageAttribute;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import controllers.ImageBrowser;

public class PodbaseUtil {
	
	private static class ImageAttributeAdaptor implements JsonSerializer<ImageAttribute> {
		@Override
		public JsonElement serialize(ImageAttribute attr, Type type, JsonSerializationContext context) {
			JsonObject obj = new JsonObject();
			obj.addProperty("id", attr.id);
			obj.add("created", context.serialize(attr.created));
			obj.add("modified", context.serialize(attr.modified));
			obj.addProperty("attribute", attr.attribute);
			obj.addProperty("value", attr.value);
			obj.addProperty("data", attr.data);
			obj.addProperty("hidden", attr.hidden);
			obj.addProperty("linkedAttribute", attr.linkedAttribute != null ? attr.linkedAttribute.id : null);
			obj.addProperty("templated", attr.templated);
			return obj;
		}
	}

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
			}}).registerTypeAdapter(ImageAttribute.class, new ImageAttributeAdaptor()).create();
		return gson;
	}

}
