/*
 * Copyright (c) 2016 droidwolf(droidwolf2006@gmail.com)
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.droidwolf.sqlitedao;

public class MyColumn {
	boolean primaryKey = false;
	boolean unique = false;
	boolean autoInc = false;
	boolean notNull = false;
	String defaultValue = null;
	String checkValue = null;
	int minSize,maxSize;

	public boolean primaryKey() {
		return primaryKey;
	}

	public boolean unique() {
		return unique;
	}

	public boolean autoInc() {
		return autoInc;
	}

	public boolean notNull() {
		return notNull;
	}

	public String defaultValue() {
		return defaultValue;
	}

	public String checkValue() {
		return checkValue;
	}
	
	public int minSize(){
		return minSize;
	}
	
	public int maxSize(){
		return maxSize;
	}
	
	public int size(){
		return minSize;
	}
}// end class MyColumn
