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
import java.util.HashMap;
import java.util.Map;

public class Lexer  {
    public final static String[] TOKENS = { "CREATE", "TABLE", "UNIQUE",
	    "NULL", "PRIMARY", "KEY", "NOT", "AUTOINCREMENT", "DEFAULT",
	    "CHECK", "IF", "EXISTS", "TEMP", "TEMPORARY", "EOF", "SEMI", "ID",
	    "INT", "FLOAT", "STING", "SL_COMMENT", "ML_COMMENT", "LPAREN", "RPAREN", "LBRACKET", "RBRACKET", "COMMA" };

    // token type
    public static final int CREATE = 0, TABLE = 1, UNIQUE = 2, NULL = 3,
	    PRIMARY = 4, KEY = 5, NOT = 6, AUTOINCREMENT = 7, DEFAULT = 8,
	    CHECK = 9, IF = 10, EXISTS = 11, TEMP = 12, TEMPORARY = 13,
	    EOF = 14, SEMI = EOF + 1, ID = EOF + 2, INT = EOF + 3,
	    FLOAT = EOF + 4, STRING = EOF + 5, SL_COMMENT = EOF + 6,
	    ML_COMMENT = EOF + 7, LPAREN = EOF + 8, RPAREN = EOF + 9,
	    LBRACKET = EOF + 10, RBRACKET = EOF + 11, COMMA = EOF + 12;

	private static final Map<String,Integer> KEYWORDS=new HashMap<String,Integer>(TEMPORARY-CREATE+1);
	//
	private final String mInput;
	private int pIn;						//��ָ��
	private int mP;						//��ǰ��ָ��
	private char[] mBuffer;			//��
	private final int K;					//��ǰ��k
	private static final char END = (char) -1;
	
	static{
	    for(int i=CREATE;i<=TEMPORARY;i++){
	    	KEYWORDS.put(TOKENS[i], i);
	    }
	}
	public Lexer(String input, int k) {
		mInput = input;
		K = k;
		mBuffer = new char[k];
		consume(k);
	}
	//
	
	public Lexer(String input) {
		this(input,2);
	}

	public Token nextToken() throws Exception  {
		char ch,lc2;
		while ((ch = LC(1)) != END) {
			switch (ch) {
				case ' ' : case '\t' : case '\n' : case '\r' :
					doWS();
					continue;
				case ',' :
					consume();
					return new Token(COMMA, ",");
				case '(' :
					consume();
					return new Token(LPAREN, "(");
				case ')' :
					consume();
					return new Token(RPAREN, ")");
				case '[' :
					consume();
					return new Token(LBRACKET, "[");
				case ']' :
					consume();
					return new Token(RBRACKET, "]");
				case ';' :
					consume();
					return new Token(SEMI, ";");
				case '\'' :
					return new Token(STRING, getSTR());
				case '_' :
				    	return scanID();
				case '-':
					 lc2 = LC(2);
					 if(lc2=='-'){
						 doSLC();
					 }else if(isDigit(lc2)){
						 return scanNumber();
					 }else{
						throw new Exception("-��<"+lc2 +">�޷�ʶ��!");
					}
					break;
				case '/':
					lc2 = LC(2);
					if(lc2=='*'){
						doMLC();
					}else{
						throw new Exception("/��<"+lc2 +">�޷�ʶ��!");
					}
					break;
				case '.':
						if( isDigit(LC(2))){
							return scanNumber();
						}else{
							throw new Exception(LC(2)+":��������!" );
						}
				case '0':case '1':case '2':case '3':case '4':case '5':case '6':case '7':case '8':case '9':
				    return scanNumber();
				default :
				    if(isLetter(ch) ){
					return scanID();
				    }else{
					throw new Exception("unknow char:"+ch );
				    }
			}
		}
		return new Token(EOF, "EOF");
	}

	private Token scanNumber() throws Exception{
		StringBuffer sb = new StringBuffer();
		boolean iF=false,iNegative =false;
		for(;;){
			char ch=LC(1) ;
			if( ch=='.' ){
				if(isDigit(LC(2))){
					iF=true;
				}else{
					throw new Exception("����ĵ����!");
				}
			}else if( ch=='-' && !iNegative){
				iNegative=true;
			}else if( !isDigit(ch)){
				break;
			}
			sb.append(ch);
			consume();
		}
		return new Token( iF? FLOAT:INT,sb.toString());
	}
			
	private Token scanID() {
		StringBuffer sb = new StringBuffer();
		char ch;
		do {
			sb.append(LC(1));
			consume();
			ch = LC(1);
		} while (isLetter(ch) || isUL(ch) || isDigit(ch));
		String id = sb.toString();
		return lookup(id);
	}

	private Token lookup(String str){
	    Integer type=KEYWORDS.get(str.toUpperCase());
	    return new Token(type==null? ID:type, str);
	}
	
	private String getSTR() throws Exception {
		consume();
		StringBuffer sb = new StringBuffer();
		char ch ;
		for(;;) {
			ch=LC(1) ;
			if(ch=='\''){
				break;
			}else if (ch=='\r'||ch=='\n'|| ch==END){
				throw new Exception("δʶ���ַ�����������!");
			}else if (ch=='\\' &&  LC(2) =='\''){
				sb.append("\\'");
				consume(2);
			}else{
				sb.append(ch);
				consume();
			}
		}
		consume();
		return sb.toString();
	}
	
	private void doSLC(){
		consume(2);
		char ch;
		while( (ch=LC(1))!='\r' &&  ch!='\n' && ch!=END){
			consume();
		}
		LOG("SLComment");
	}
	
	private void doMLC() throws Exception{
		consume(2);
		for (;;) {
			char lc1 = LC(1),lc2 = LC(2);
			if( lc1=='*' && lc2=='/'){
				break;
			}else if (lc1 == END || lc2 == END) {
				throw new Exception("δʶ��ע�ͽ�����!");
			}
			consume();
		}
		consume(2);
		LOG("MLComment");
	}
	
	private void doWS() {
		while (isWS()) {
			consume();
		}
	}

	private boolean isUL(char ch){
		return ch=='_';
	}
	private boolean isDigit(char ch) {
		return (ch >= '0' && ch <= '9');
	}

	private boolean isLetter(char ch) {
		return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z');
	}

	private boolean isWS() {
		char ch = LC(1);
		return ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r';
	}
	
	//
	private char NC() {
		return pIn >= mInput.length()? END:mInput.charAt(pIn++);
	}

	private char LC(int i) {
		return mBuffer[(mP + i - 1) % K];
	}

	private void consume(int n) {
		for (int i = 0; i < n; i++) {
			mBuffer[mP] = NC();
			mP = (mP + 1) % K;
		}
	}

	public String getInvalidString(){
		int n=pIn;
		if(n<mInput.length())
			return mInput.substring(n);
		return "";
	}
	
	private void consume() {
		consume(1);
	}
	//
	private void LOG(String text){
		System.out.println(text);
	}
}// end class
