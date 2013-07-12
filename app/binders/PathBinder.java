// Copyright 2013 Julian Hartline <julianh2o@gmail.com>
// 
// This code is available under the MIT license.
// See the LICENSE file for details.
package binders;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.file.Path;

import play.data.binding.Global;
import play.data.binding.TypeBinder;
import services.PathService;

@Global
public class PathBinder implements TypeBinder<Path> {
	@Override
	public Object bind(String name, Annotation[] annotations, String value, Class actualClass, Type genericType) throws Exception {
		if ("undefined".equals(value)) throw new IllegalArgumentException("Path is undefined!");
		return PathService.resolve(value);
	}
}
