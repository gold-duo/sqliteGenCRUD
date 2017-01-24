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
package com.droidwolf.sqliteparser;
import java.util.ArrayList;
import java.util.List;


public class TableNode extends Node{

	public boolean mTemporary;
	private List<ColumnNode> mColumnNodes= new ArrayList<ColumnNode>();
	public TableNode(Token tk,boolean isTemp) {
		super(tk);
		mTemporary=isTemp;
	}

	public String getName(){
		return token.text;
	}
	
	public String toString() {
		return "tbl--"+(mTemporary? "temporaray ":"")+getName();
	}
	
	public boolean isTemporary(){
		return mTemporary;
	}
	
	public void addColumnNode(ColumnNode cn){
		mColumnNodes.add(cn);
	}
	
	public List<ColumnNode> getColumnNodes(){
		return mColumnNodes;
	}
}
