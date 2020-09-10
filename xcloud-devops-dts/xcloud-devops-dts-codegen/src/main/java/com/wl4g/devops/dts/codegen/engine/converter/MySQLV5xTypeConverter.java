/*
 * Copyright 2017 ~ 2025 the original author or authors. <wanglsir@gmail.com, 983708408@qq.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wl4g.devops.dts.codegen.engine.converter;

import static com.wl4g.devops.dts.codegen.utils.ResourceBundleUtils.readResource;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

/**
 * {@link MySQLV5xTypeConverter}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020-09-10
 * @since
 */
public class MySQLV5xTypeConverter extends DbTypeConverter {

	@Override
	public DbConverterType kind() {
		return DbConverterType.MySQL;
	}

	@Override
	public String convertToJavaType(String sqlType) {
		return sqlToJavaTypes.getProperty(sqlType);
	}

	@Override
	public String convertToSqlType(String javaType) {
		return javaToSqlTypes.getProperty(javaType);
	}

	// Cache of types.
	private final static Properties sqlToJavaTypes;
	private final static Properties javaToSqlTypes;
	private final static String TYPE_MYSQL = "mysql";

	static {
		try {
			// sql to java
			sqlToJavaTypes = new Properties();
			sqlToJavaTypes.load(new StringReader(readResource(false, TYPES_BASE_PATH, TYPE_MYSQL, TYPES_SQL_TO_JAVA)));

			// java to sql
			javaToSqlTypes = new Properties();
			javaToSqlTypes.load(new StringReader(readResource(false, TYPES_BASE_PATH, TYPE_MYSQL, TYPES_JAVA_TO_SQL)));
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException(e);
		}

	}

}