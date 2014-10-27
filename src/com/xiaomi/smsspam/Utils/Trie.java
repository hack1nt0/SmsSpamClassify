package com.xiaomi.smsspam.Utils;

import java.util.List;

public class Trie {
	TrieNode root = new TrieNode('$');
	
	public void buildTrie(List<String> dictList)
	{
		for(String term :  dictList)
		{
			insertTrie(root,term);
		}
	}
	
	public void insertTrie(TrieNode tn,String term)
	{
				
		char c = term.charAt(0);
		
		TrieNode n = tn.childs.get(c);
		
		int len = term.length();		
		
		if(n == null)
		{
			n = new TrieNode(c);
			tn.childs.put(c, n);			
		}
		if(len == 1)
		{
			n.bound = true;
		}
		else
		{
			insertTrie(n,term.substring(1,term.length()));
		}
	}
	
	public int inDict(String term)
	{
		//-1：不在字典中，也不是前缀
		//0: 不在字典中，是前缀
		//1：在字典中，不是前缀
		//2: 在字典中，是前缀
		int ret = 0;
		TrieNode tn = root;
		char c;
		int i;
		for(i = 0 ; i < term.length()-1 ; i++)
		{
			c = term.charAt(i);
			TrieNode n = tn.childs.get(c);
			if(n == null)
			{
				ret = -1;
				break;
			}
			else
			{
				tn = n;
			}			
		}
		if(i == term.length()-1)
		{
			c = term.charAt(i);
			TrieNode n = tn.childs.get(c);
			if(n == null)
			{
				ret = -1;
			}			
			else if(n.childs.isEmpty() && n.bound == true)
			{
				ret = 1;
			}
			else if(!n.childs.isEmpty() && n.bound == false)
			{
				ret = 0;
			}
			else if(!n.childs.isEmpty() && n.bound == true)
			{
				ret = 2;
			}
		}
		return ret;
	}

}
