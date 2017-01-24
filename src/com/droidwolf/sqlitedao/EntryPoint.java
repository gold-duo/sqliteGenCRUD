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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.droidwolf.sqliteparser.ColumnNode;
import com.droidwolf.sqliteparser.Lexer;
import com.droidwolf.sqliteparser.Parser;
import com.droidwolf.sqliteparser.TableNode;

public class EntryPoint {

	public static void main(String[] args) {
		EntryPoint ep= new EntryPoint();
		ep.execute();
	}
	
	private void execute(){
		for(;;){
			TableNode tblNode = getCreateTableStat();
			if(tblNode==null || tblNode.getColumnNodes().isEmpty()){
				if( checkConitue()){
					continue;
				}else{
					break;
				}
			}
			
			Map<MyField, MyColumn> fieldCols = tranColNode2Field(tblNode.getColumnNodes());
			List<MyField> fields = getFields(fieldCols);

			String path = getInputPath();
			boolean aliasColname = false, boolFalseSet2Null = false;
			aliasColname = aliasColname();
			if (hasBoolCol(fields)) {
				boolFalseSet2Null = boolFalseSet2Null();
			}
			
			//dao
			new CRUDGenerator(tblNode.getName(), fieldCols, aliasColname,boolFalseSet2Null).generate(path);
			
			//entity
			if(outputEntityClass(tblNode.getName())){
				new EntityGenerator(tblNode.getName(), fields).generate(path);
			}
			if( checkConitue()){
				continue;
			}else{
				break;
			}
		}
	}
	
	private boolean hasBoolCol(List<MyField> fields){
		for(int i=0;i<fields.size();i++){
			if( fields.get(i).getType().equals(boolean.class)){
				return true;
			}
		}
		return false;
	}
	
	private String getInputPath(){
		for(int i=0;i<3;i++){
			System.out.println("��������������ļ�Ŀ¼�س�...");
			String str=readInputString(System.in);
			File file= new File(str);
			if( file.exists()&&file.isDirectory()){
				return str;
			}
		}
		return null;
	}
	
	private boolean checkConitue(){
		System.out.println("�Ƿ������Y�س�����,�����˳�...");
		String str=readInputString(System.in);
		return str!=null && str.length()>0&& str.trim().toLowerCase().equals("y");
	}
	
	private boolean outputEntityClass(String tbl){
		System.out.println("�Ƿ�����"+tbl+"ʵ���ࣿY�س����ɣ���������");
		String str=readInputString(System.in);
		return str!=null && str.length()>0&& str.trim().toLowerCase().equals("y");
	}
	
	private boolean aliasColname(){
		System.out.println("�Ƿ����������Y�س����������򲻻���");
		String str=readInputString(System.in);
		return str!=null && str.length()>0&& str.trim().toLowerCase().equals("y");
	}
	
	private boolean boolFalseSet2Null(){
		System.out.println("�Ƿ�boolean�ֶε�falseֵ���ó�null��Y�س��ǣ�������");
		String str=readInputString(System.in);
		return str!=null && str.length()>0&& str.trim().toLowerCase().equals("y");
	}
	
	private TableNode getCreateTableStat(){
		List<TableNode> tblNodes=null;
		System.out.println("������sqlite create table ����س�...");
		String sql= readInputString(System.in);
		if(sql!=null&& sql.length()>0){
			try {
				Parser parser= new Parser(new Lexer(sql));
				parser.createTableStatement();
				tblNodes=parser.getTableNodes();
			} catch (Exception e) {
				System.out.println("����sqlite������"+e.getMessage());
			}
		}
		return tblNodes==null||tblNodes.isEmpty()? null:tblNodes.get(0);
	}
	
	private Map<MyField,MyColumn> tranColNode2Field( List<ColumnNode> nodes){
		Map<MyField,MyColumn> map= new HashMap<MyField,MyColumn>(nodes.size());
		for(int i=0;i<nodes.size();i++){
			ColumnNode node= nodes.get(i);
			MyColumn mc= new MyColumn();
			MyField field= new MyField(node.getName(), tranTypeFromString(node.getType()));
			
//			mc.checkValue=node.
			mc.minSize=node.getTypeMinSize();
			mc.maxSize=node.getTypeMaxSize();
			
			mc.autoInc=node.isContraint(ColumnNode.AUTO);
			mc.unique=node.isContraint(ColumnNode.UNIQUE);
			mc.primaryKey=node.isContraint(ColumnNode.PRIMARYKEY);
			mc.notNull=node.isContraint(ColumnNode.NOTNULL);
			if(node.isContraint(ColumnNode.DEFAULT)){
				mc.defaultValue=node.getDefaultVaule();
			}
			
			map.put(field, mc);
		}
		return map;
	}
	
	private Class<?> tranTypeFromString(String id){
		Class<?> ret=null;
		if (id.equals("int")) {
			ret=int.class;
		} else if (id.equals("boolean")) {
			ret=boolean.class;
		} else if (id.equals("long")) {
			ret=long.class;
		} else if (id.equals("short")) {
			ret=short.class;
		} else if (id.equals("float")) {
			ret=float.class;
		} else if (id.equals("double")) {
			ret=double.class;
		} else if (id.equals("[]byte")) {
			ret=byte[].class;
		} else if (id.equals("string") ) {
			ret=String.class;
		}
		return ret;
	}
	
	private List<MyField> getFields(Map<MyField,MyColumn> map){
		List<MyField> list= new ArrayList<MyField>(map.size());
		for(MyField key:map.keySet()){
			list.add(key);
		}
		return list;
	}

	private String readInputString(InputStream is) {
		BufferedReader buffer = null;
		StringBuilder sb =new StringBuilder();
		try {
			buffer = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = buffer.readLine()) != null) {
				sb.append(line);
				break;
			}
//			buffer.close();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		} finally {
//			if (is != null)
//				try {
//					is.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			if(buffer!=null)
//				try {
//					buffer.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
		}
		return sb.toString();
	}
}
