// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
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
import java.util.Date;

import org.apache.commons.codec.binary.Base64;

import models.Activation;
import models.DatabaseImage;
import models.GsonTransient;
import models.ImageAttribute;
import models.Project;
import access.AccessType;
import access.VirtualAccessType;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
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
	
	private static class AccessTypeAdaptor implements JsonSerializer<AccessType> {
		@Override
		public JsonElement serialize(AccessType obj, Type type, JsonSerializationContext context) {
			JsonObject json = new JsonObject();
			json.addProperty("name", obj.name());
			json.addProperty("description", obj.description);
			json.addProperty("aggregate", obj.aggregate);
			json.add("type", context.serialize(obj.types));
			return json;
		}
	}
	
	private static class VirtualAccessTypeAdaptor implements JsonSerializer<VirtualAccessType> {
		@Override
		public JsonElement serialize(VirtualAccessType obj, Type type, JsonSerializationContext context) {
			JsonObject json = new JsonObject();
			json.addProperty("name", obj.name());
			json.addProperty("description", obj.getDescription());
			json.add("impliedBy", context.serialize(obj.impliedBy));
			json.add("type", context.serialize(obj.type.types));
			return json;
		}
	}
	
	private static class ActivationAdaptor implements JsonSerializer<Activation> {
		@Override
		public JsonElement serialize(Activation obj, Type type, JsonSerializationContext context) {
			JsonObject json = new JsonObject();
			json.addProperty("id", obj.id);
			json.addProperty("activationCode", obj.activationCode);
			json.addProperty("expirationDate", obj.expirationDate.getTime());
			return json;
		}
	}
	
	private static class DateAdaptor implements JsonSerializer<Date> {
		@Override
		public JsonElement serialize(Date obj, Type type, JsonSerializationContext context) {
			return new JsonPrimitive(obj.getTime());
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
			}})
			.registerTypeAdapter(ImageAttribute.class, new ImageAttributeAdaptor())
			.registerTypeAdapter(AccessType.class, new AccessTypeAdaptor())
			.registerTypeAdapter(VirtualAccessType.class, new VirtualAccessTypeAdaptor())
			.registerTypeAdapter(Activation.class, new ActivationAdaptor())
			.registerTypeAdapter(Date.class, new DateAdaptor())
			.create();
		return gson;
	}

	public static String argumentString(Object... arguments) {
		StringBuilder hashable = new StringBuilder();
		for (Object o : arguments) {
			hashable.append(o == null ? "NULL" : o.toString());
			hashable.append(",");
		}
		return hashable.toString();
	}

	public static String argumentHash(Object... arguments) {
		return new String(Base64.encodeBase64(org.apache.commons.codec.digest.DigestUtils.md5(argumentString(arguments).getBytes())));
	}
}
