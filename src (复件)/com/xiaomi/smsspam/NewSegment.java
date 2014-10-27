package com.xiaomi.smsspam;


import com.xiaomi.smsspam.Utils.Trie;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

public class NewSegment {
	
	public boolean mInit = false;
	private List<String> mDictList = new ArrayList<String>();
	Trie trie = new Trie();
	
	public void readDict()
	{
		try {
			InputStream ins = new FileInputStream(Options.NEW_DICT);
			BufferedReader br = new BufferedReader(new InputStreamReader(ins));
			String str;
			while((str = br.readLine()) != null)
			{
				mDictList.add(str);
			}			
			br.close();
			ins.close();
			
			mInit = true;
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}
	
//	public void init()
//	{
//		if(!mDictList.isEmpty())
//		{
//			trie.buildTrie(mDictList);		
//			mInit = true;
//		}		
//	}
	
	public void init(List<String> ls)
	{		
		if(!ls.isEmpty())
		{
			mDictList = ls;
			trie.buildTrie(mDictList);		
			mInit = true;
		}	
		
	}
	
	public List<String> cut(String res){
		List<String> ret = new ArrayList<String>();
		
		int i = 0;
		int j = 0;
		int type = 0;
		for(i = 0 ; i < res.length() ; i++)
		{
			List<String> termList = new ArrayList<String>();
			j = i+1;
			String term;
			do
			{
				term = res.substring(i,j);
				type = trie.inDict(term);
				if(type == 1 || type == 2)
				{
					termList.add(term);
				}
				j++;
			}while((type == 0 || type == 2) && j <= res.length());
			//
//			if(j == i+2 && type == -1)
//			{
//				ret.add(term);
//			}
			//
			if(!termList.isEmpty())
			{
				term = termList.get(termList.size()-1);
				if(!term.isEmpty())
				{
					ret.add(term);
					i = i+term.length()-1;
				}
			}
			
		}
		
		return ret;
        
    }

}
