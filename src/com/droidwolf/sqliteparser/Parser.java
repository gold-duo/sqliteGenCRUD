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
import java.util.List;
import java.util.ArrayList;

public class Parser {
	private final Lexer mLexer;
	private Token []mTokens;		//词法向前看缓冲
	private int P;							//词法向前看指针
	private final int K;					//向前看k
	
	private List<TableNode> mTableNodes= new ArrayList<TableNode>();
	
	private TableNode mTableNode=null;
	private ColumnNode mColumnNode=null;
	public Parser(Lexer lexer) throws Exception{
		mLexer=lexer;
		K=1;
		mTokens = new Token[K];
		consume(K);
	}
	
	public void createTableStatement() throws Exception{
		boolean isTemporary=false;
		//create (temp|temporary)? table
		match(Lexer.CREATE);
		if(!tryMatch(Lexer.TEMP)){
			isTemporary=tryMatch(Lexer.TEMPORARY);
		}else{
			isTemporary=true;
		}
		match(Lexer.TABLE);
		
		// if not exists
		if(tryMatch(Lexer.IF)){
			match(Lexer.NOT);
			match(Lexer.EXISTS);
		}
		
		//table name
		tblName(isTemporary);
		
		columnList();
		
		tryMatch(Lexer.SEMI);
		match(Lexer.EOF);
	}
	
	private void columnList() throws Exception{
		match(Lexer.LPAREN);
		column();
		while(tryMatch(Lexer.COMMA)){
			column();
		}
		match(Lexer.RPAREN);
	}
	
	private void column() throws Exception{
		colName();
		colType();// type
		
		int LA;
		while ((LA = LA(1)) == Lexer.PRIMARY || LA == Lexer.UNIQUE
				|| LA == Lexer.AUTOINCREMENT || LA == Lexer.DEFAULT
				|| LA == Lexer.NOT) {
			if (LA == Lexer.PRIMARY) {
				contraintPrimaryKey();// test primary key
			} else if (LA == Lexer.UNIQUE) {
				contraintUnique();// test unique
			} else if (LA == Lexer.AUTOINCREMENT) {
				contraintAutoincrement();// test Autoincrement
			} else if (LA == Lexer.DEFAULT) {
				contraintDefault(); // test default
			} else if (LA == Lexer.NOT) {
				contraintNotNull(); // test not null
			}
		}
	}
	
	
	private void tblName(boolean isTemporary) throws Exception {
		Token token=null;
		if (tryMatch(Lexer.LBRACKET)) {
			if(LA(1)== Lexer.ID){
				token=LT(1);
//				token.text="["+token.text+"]";
				consume();
				match(Lexer.RBRACKET);
			}
		} else if (LA(1) == Lexer.ID) {
			token=LT(1);
			consume();
		}
		
		if(token==null){
			throw new Exception(LT(1).text+":无法识别成表名!"+ "\n"+mLexer.getInvalidString());
		}else{
			mTableNode=new TableNode(token,isTemporary);
			mTableNodes.add( mTableNode);
		}
	}
	
	private void colName() throws Exception {
		Token token=null;
		if (tryMatch(Lexer.LBRACKET)) {
			if(LA(1)== Lexer.ID){
				token=LT(1);
//				token.text="["+token.text+"]";
				consume();
				match(Lexer.RBRACKET);
			}
		} else if (LA(1) == Lexer.ID) {
			token=LT(1);
			consume();
		}
		
		if(token==null){
			throw new Exception(LT(1).text+":无法识别成表名!"+ "\n"+mLexer.getInvalidString());
		}else{
			mColumnNode = new ColumnNode(token);
			mTableNode.addColumnNode(mColumnNode);
		}
	}
	
	private void colType() throws Exception {
		Token tk = LT(1);
		if (tk.type == Lexer.ID && isType(tk.text)) {
//			LOG("colType--" + tk.text);
			mColumnNode.setType(tk.text);
			consume();

			if ( tryMatch(Lexer.LPAREN) ) {
				if (LA(1) == Lexer.INT) {
					mColumnNode.setTypeMinSize(Integer.parseInt(LT(1).text));
					consume();

					if ( tryMatch(Lexer.COMMA)) {
						if (LA(1) == Lexer.INT) {
							mColumnNode.setTypeMaxSize(Integer.parseInt(LT(1).text));
							consume();
						} else {
							throw new Exception(LT(1).text+ ":不是数字！"+ "\n"+mLexer.getInvalidString());
						}
					}
				} else {
					throw new Exception(LT(1).text+ ":不是数字！"+ "\n"+mLexer.getInvalidString());
				}
				match(Lexer.RPAREN);
			}
		} else {
			throw new Exception("无法识别类型:" + tk.text + "\n"+mLexer.getInvalidString());
		}
	}
	
	private boolean isType(String id){
		id=id.toLowerCase();
		return id.equals("int")|| id.equals("integer") 
				||id.equals("bool")||id.equals("boolean")
				||id.equals("long")||id.equals("numeric")
				||id.equals("short")||id.equals("byte")
				||id.equals("float")
				||id.equals("real")||id.equals("double")
				||id.equals("blob")
				||id.equals("text")||id.equals("varchar")||id.equals("nvarchar")||id.equals("string")||id.equals("char");
	}
	
	private boolean contraintPrimaryKey() throws Exception{
		if(tryMatch(Lexer.PRIMARY)){
			if (LA(1) == Lexer.KEY) {
				mColumnNode.setContraint(ColumnNode.PRIMARYKEY);
				consume();
//				LOG("contraintPrimaryKey");
				return true;
			}else{
				throw new Exception("PRIMARY KEY关键字[KEY]丢失！"+ "\n"+mLexer.getInvalidString());
			}
		}
		return false;
	}
	
	private boolean contraintUnique() throws Exception{
		if(LA(1)==Lexer.UNIQUE){
			mColumnNode.setContraint(ColumnNode.UNIQUE);
			consume();
//			LOG("contraintUnique");
			return true;
		}
		return false;
	}
	
	private boolean contraintAutoincrement() throws Exception{
		if(LA(1)==Lexer.AUTOINCREMENT){
			mColumnNode.setContraint(ColumnNode.AUTO);
			consume();
//			LOG("contraintAutoincrement");
			return true;
		}
		return false;
	}
	
	private boolean contraintNotNull() throws Exception{
		if(tryMatch(Lexer.NOT)){
			if( LA(1)==Lexer.NULL){
				mColumnNode.setContraint(ColumnNode.NOTNULL);
				consume();
//				LOG("contraintNotNull");
				return true;
			}else{
				throw new Exception("NOT NULL关键字[NULL]丢失！"+ "\n"+mLexer.getInvalidString());
			}
		}
		return false;
	}
	
	private void defaultVaule() throws Exception{
		Token tk=LT(1);
		int la =tk.type;
		if (la==Lexer.STRING||la==Lexer.INT||la==Lexer.FLOAT){
			mColumnNode.setDefaultValue(la==Lexer.STRING?  "'"+tk.text+"'": tk.text);
			consume();
		}else{
			throw new Exception(tk.text+":default 值不为数字或字符串!"+ "\n"+mLexer.getInvalidString());
		}
	}
	
	private boolean contraintDefault() throws Exception{
		boolean ret =true;
		if(tryMatch(Lexer.DEFAULT)){
			if(tryMatch(Lexer.LPAREN)){
				defaultVaule();
				match(Lexer.RPAREN);
			}else{
				defaultVaule();
			}
			mColumnNode.setContraint(ColumnNode.DEFAULT);
		}else{
			ret=false;
		}
		return ret;
	}
	
//	private void contraintCheck(){
//		
//	}
	
	private Token LT(int i){
		return mTokens[(P+i-1)%K];
	}
	
	private int LA(int i){
		return LT(i).type;
	}
	
	private boolean tryMatch(int type) throws Exception{
		if ( LA(1) == type ) {
			consume();
			return true;
		}
		return false;
	}
	
	private void match(int type) throws Exception {
        if ( LA(1) == type ) {
//        	LOG("match--"+ LT(1));
        	consume();
        } else{
        	throw new Exception(LT(1) .text+" 不匹配 "+Lexer.TOKENS[type]+ "\n"+mLexer.getInvalidString());
        }
                             
    }
	
	private void consume() throws Exception {
    	consume(1);
    }
    
	private void consume(int n) throws Exception {
		for (int i = 0; i < n; i++) {
			mTokens[P] = mLexer.nextToken();
			P = (P + 1) % K;
		}
	}
	
	private void LOG(String text) {
		// System.out.println(text);
	}

	public List<TableNode> getTableNodes(){
		return mTableNodes;
	}
	//--
}//end class
