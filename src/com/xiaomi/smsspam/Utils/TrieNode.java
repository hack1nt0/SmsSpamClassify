package com.xiaomi.smsspam.Utils;

import java.util.HashMap;
/**
 * 构建内存词典的Trie树结点
 * 
 * @author single(宋乐)
 * @version 1.01, 10/11/2009
 */
public class TrieNode {

	/**结点关键字，其值为中文词中的一个字*/
	public char key=(char)0;
	/**如果该字在词语的末尾，则bound=true*/
	public boolean bound=false;
	/**指向下一个结点的指针结构，用来存放当前字在词中的下一个字的位置*/
	public HashMap<Character,TrieNode> childs=new HashMap<Character,TrieNode>();
	
	public TrieNode(){
	}
	
	public TrieNode(char k){
		this.key=k;
	}
}

