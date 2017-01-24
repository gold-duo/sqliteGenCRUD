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
import java.util.BitSet;


public class ColumnNode extends Node{
	public final static int NONE=0, AUTO=1,PRIMARYKEY=2,UNIQUE=3,NOTNULL=4,DEFAULT=5;
	private BitSet mContraint;
	private String mType="";
	private String mDefaultValue=null;
	private int mTypeMinSize,mTypeMaxSize;
	public ColumnNode(Token tk){
		super(tk);
	}
	public ColumnNode(Token tk,String type,int contraintFlag) {
		super(tk);
		setContraint(contraintFlag);
		setType(type);
	}

	public String getName(){
		return token.text;
	}
	
	public void setContraint(int c){
		if(c!=NONE){
			if(mContraint==null)mContraint= new BitSet();
			mContraint.set(c);
		}
	}
	
	public boolean isContraint(int c){
		if(mContraint!=null){
			return mContraint.get(c);
		}
		return false;
	}
	
	public void setDefaultValue(String val){
		mDefaultValue=val;
	}
	public String getDefaultVaule(){
		return mDefaultValue;
	}
	
	public String getType(){
		String ret="";
//		if( mTypeMaxSize!=0&& mTypeMinSize!=0){
//			ret=mType+"("+mTypeMinSize+","+mTypeMaxSize+")";
//		}else if(  mTypeMinSize!=0){
//			ret=mType+"("+mTypeMinSize+")";
//		}else {
			ret=mType;
//		}
		return ret;
	}
	
 	public void setType(String id) {
		id=id.toLowerCase();
		if (/*id.equals("int") || */id.equals("integer")) {
			id="int";
		} else if (id.equals("bool") /*|| id.equals("boolean")*/) {
			id="boolean";
		} else if (/*id.equals("long")||*/id.equals("numeric")) {
			id="long";
		} else if (/*id.equals("short") || */id.equals("byte")) {
			id="short";
//		} else if (id.equals("float")) {
//			
		} else if (id.equals("real") /*||id.equals("double")*/) {
			id="double";
//			
		} else if (id.equals("blob")) {
			id="[]byte";
		} else if (id.equals("text") || id.equals("varchar")
				|| id.equals("nvarchar")/*|| id.equals("string")*/||id.equals("char")) {
			id="string";
		}
		mType=id;
	}
 	
 	public void setTypeMinSize(int val){
 		mTypeMinSize=val;
 	}
	public void setTypeMaxSize(int val){
 		mTypeMaxSize=val;
 	}
 	public int getTypeMinSize(){
 		return mTypeMinSize;
 	}
	public int getTypeMaxSize(){
		return mTypeMaxSize;
 	}
	
	@Override
	public String toString() {
		return "Column--"+ getName()+" " + getType() +getContraint();
	}
	
	private String getContraint(){
		StringBuffer str=new StringBuffer();
		if(isContraint (AUTO)){
			str.append(" autoincrement");
		}
		if(isContraint (PRIMARYKEY)){
			str.append(" primary key");
		}
		if(isContraint (UNIQUE)){
			str.append(" unique");
		}
		if(isContraint (DEFAULT)){
			str.append(" default(").append(getDefaultVaule() ).append(")");
		}
		if(isContraint (NOTNULL)){
			str.append(" not null");
		}
		return str.toString();
	}
}
