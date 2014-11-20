/************************************
* file enc : ASCII
* author   : wuyanyi09@gmail.com
************************************/
#ifndef CPPJIEBA_TRIE_H
#define CPPJIEBA_TRIE_H

class iterator;

#include <iostream>
#include <fstream>
#include <map>
#include <algorithm>
#include <cstring>
#include <stdint.h>
#include <cmath>
#include <math.h>
#include <limits>
#include "Limonp/str_functs.hpp"
#include "Limonp/logger.hpp"
#include "TransCode.hpp"



namespace CppJieba
{
    using namespace Limonp;
    const double MIN_DOUBLE = -3.14e+100;
    const double MAX_DOUBLE = 3.14e+100;
    //typedef unordered_map<uint16_t, struct TrieNode*> TrieNodeMap;
    typedef vector<struct TrieNode> TrieNodeMap;
    struct TrieNode
    {
        TrieNodeMap hmap;
        bool isLeaf;
        uint nodeInfoVecPos;
        uint16_t what;
        TrieNode()
        {
            isLeaf = false;
            nodeInfoVecPos = -1;
        }

        bool operator < (const TrieNode& b) const{
            return what < b.what;//TODO
        }

        /*
        bool operator > (const TrieNode& b) const {
            return what > b.what;//TODO
        }*/


    };

    struct TrieNodeInfo
    {
        Unicode word;
        size_t freq;
        double logInfoGains;
        string tag;
        double logFreq; //logFreq = log(freq/sum(freq));
        TrieNodeInfo():freq(0),logFreq(0.0), logInfoGains(0.0)
        {
        }
        TrieNodeInfo(const TrieNodeInfo& nodeInfo):word(nodeInfo.word), freq(nodeInfo.freq), tag(nodeInfo.tag), logFreq(nodeInfo.logFreq), logInfoGains(nodeInfo.logInfoGains)
        {
        }
        TrieNodeInfo(const Unicode& _word):word(_word),freq(0),logFreq(MIN_DOUBLE),logInfoGains(MIN_DOUBLE)
        {
        }
        bool operator == (const TrieNodeInfo & rhs) const
        {
            return word == rhs.word && freq == rhs.freq && tag == rhs.tag && abs(logFreq - rhs.logFreq) < 0.001 && abs(logInfoGains - rhs.logInfoGains) < 0.001;
        }
    };

    inline ostream& operator << (ostream& os, const TrieNodeInfo & nodeInfo)
    {
        return os << nodeInfo.word << ":" << nodeInfo.freq << ":" << nodeInfo.tag << ":" << nodeInfo.logFreq << ":" << nodeInfo.logInfoGains;
    }

    typedef unordered_map<uint, const TrieNodeInfo*> DagType;

    class Trie {

    private:
        TrieNode* _root;
        TrieNode* tmp;
        vector<TrieNodeInfo> _nodeInfoVec;

        bool _initFlag;
        int64_t _freqSum;
        double _minLogFreq;
        double _minLogInfoGains;

    public:
        Trie()
        {
            _root = NULL;
            _freqSum = 0;
            _minLogFreq = MAX_DOUBLE;
            _minLogInfoGains = MAX_DOUBLE;
            _initFlag = false;
        }
        ~Trie()
        {
            dispose();
        }
        bool init()
        {
            if(_getInitFlag())
            {
                LogError("already initted!");
                return false;
            }

            try
            {
                _root = new TrieNode;
                tmp = new TrieNode;
            }
            catch(const bad_alloc& e)
            {
                return false;
            }
            if(NULL == _root)
            {
                return false;
            }
            _setInitFlag(true);
            return true;
        }
        bool dispose()
        {
            if(!_getInitFlag())
            {
                return false;
            }
            bool ret = _deleteNode(_root);
            if(!ret)
            {
                LogFatal("_deleteNode failed!");
                return false;
            }
            _root = NULL;
            _nodeInfoVec.clear();
            delete (tmp); tmp = NULL;

            _setInitFlag(false);
            return ret;
        }
        bool loadDict(const char * const filePath)
        {
            assert(_getInitFlag());
            if(!_trieInsert(filePath))
            {
                LogError("_trieInsert failed.");
                return false;
            }
            if(!_countWeight())
            {
                LogError("_countWeight failed.");
                return false;
            }
            return true;
        }

    private:
        void _setInitFlag(bool on){_initFlag = on;};
        bool _getInitFlag()const{return _initFlag;};
        bool canfind(TrieNodeMap::iterator l, TrieNodeMap::iterator r, const TrieNode& x) {
            auto ri = upper_bound(l, r, x);
            auto li = lower_bound(l, r, x);
            //return  true;
            return li < ri;
        }

    public:
        const TrieNodeInfo* find(Unicode::const_iterator begin, Unicode::const_iterator end)
        {

            if(!_getInitFlag())
            {
                LogFatal("trie not initted!");
                return NULL;
            }
            if(begin >= end)
            {
                return NULL;
            }
            TrieNode* p = _root;
            for(Unicode::const_iterator itr = begin; itr != end; itr++)
            {
                //uint16_t chUni = *it;
                //if(p->hmap.find(chUni) == p-> hmap.end())
                TrieNode tmp; tmp.what = *itr;
                if (!canfind(p->hmap.begin(), p->hmap.end(), tmp))
                {
                    return NULL;
                }
                else
                {
                   // p = p->hmap[tmp];
                    p = &(*lower_bound(p->hmap.begin(), p->hmap.end(), tmp));
                }
            }
            //if (p->isLeaf)
            if(p->nodeInfoVecPos >= 0)
            {
                uint pos = p->nodeInfoVecPos;
                if(pos < _nodeInfoVec.size())
                {
                    return &(_nodeInfoVec[pos]);
                }
                else
                {
                    LogFatal("node's nodeInfoVecPos is out of _nodeInfoVec's range");
                    return NULL;
                }
            }
            return NULL;
        }

        bool find(Unicode::const_iterator begin, Unicode::const_iterator end, vector<pair<uint, const TrieNodeInfo*> >& res)
        {
            if(!_getInitFlag())
            {
                LogFatal("trie not initted!");
                return false;
            }
            if (begin >= end)
            {
                LogFatal("begin >= end");
                return false;
            }
            TrieNode* p = _root;
            for (Unicode::const_iterator itr = begin; itr != end; itr++)
            {
                //if(p->hmap.find(*itr) == p-> hmap.end())
                //if (lower_bound(p->hmap.begin(), p->hmap.end(), *itr) == p->hmap.begin())
                //tmp->what = *itr;
                TrieNode tmp; tmp.what = *itr;
                if (!canfind(p->hmap.begin(), p->hmap.end(), tmp))
                {
                    break;
                }
                //p = p->hmap[*itr];
                p = &(*lower_bound(p->hmap.begin(), p->hmap.end(), tmp));
                //if(p->isLeaf)
                if (p->nodeInfoVecPos >= 0)
                {
                    uint pos = p->nodeInfoVecPos;
                    if(pos < _nodeInfoVec.size())
                    {
                        res.push_back(make_pair(itr-begin, &_nodeInfoVec[pos]));
                    }
                    else
                    {
                        LogFatal("node's nodeInfoVecPos is out of _nodeInfoVec's range");
                        return false;
                    }
                }
            }
            return !res.empty();
        }

        bool find(Unicode::const_iterator begin, Unicode::const_iterator end, uint offset, unordered_map<uint, const TrieNodeInfo* > & res)
        {
            if(!_getInitFlag())
            {
                LogFatal("trie not initted!");
                return false;
            }
            if (begin >= end)
            {
                LogFatal("begin >= end");
                return false;
            }
            TrieNode* p = _root;
            for (Unicode::const_iterator itr = begin; itr != end; itr++)
            {
                //if(p->hmap.find(*itr) == p-> hmap.end())
                //if (lower_bound(p->hmap.begin(), p->hmap.end(), *itr) == p->hmap.begin())
                TrieNode tmp; tmp.what = *itr;
                if (!canfind(p->hmap.begin(), p->hmap.end(), tmp))
                {
                    break;
                }
                //p = p->hmap[*itr];
                p = &(*lower_bound(p->hmap.begin(), p->hmap.end(), tmp));
                //if(p->isLeaf)
                if (p->nodeInfoVecPos >= 0)
                {
                    uint pos = p->nodeInfoVecPos;
                    if(pos < _nodeInfoVec.size())
                    {
                        //res.push_back(make_pair(itr-begin, &_nodeInfoVec[pos]));
                        res[itr-begin + offset] = &_nodeInfoVec[pos];
                    }
                    else
                    {
                        LogFatal("node's nodeInfoVecPos is out of _nodeInfoVec's range");
                        return false;
                    }
                }
            }
            return !res.empty();
        }

        bool inDict(Unicode::const_iterator begin, Unicode::const_iterator end)
        {
            if(!_getInitFlag())
            {
                LogFatal("trie not initted!");
                return false;
            }
            if (begin >= end)
            {
                LogFatal("begin >= end");
                return false;
            }
            TrieNode* p = _root;
            Unicode::const_iterator itr = begin;
            while(itr != end)
            {
                //if(p->hmap.find(*itr) == p-> hmap.end())

                TrieNode tmp; tmp.what = *itr;
                if (!canfind(p->hmap.begin(), p->hmap.end(), tmp))
                {
                    break;
                }
                //p = p->hmap[*itr];
                p = &(*lower_bound(p->hmap.begin(), p->hmap.end(), tmp));
                //if(p->isLeaf && (itr + 1 == end))
                if(p->nodeInfoVecPos >= 0 && (itr + 1 == end))
                {
                    return true;
                }
                itr++;
            }
            return false;
        }
    public:

        double getMinLogFreq()const{return _minLogFreq;};
        double getMinLogInfoGains()const{return _minLogInfoGains;};

    private:
        bool _insert(const TrieNodeInfo& nodeInfo)
        {
            if(!_getInitFlag())
            {
                LogFatal("not initted!");
                return false;
            }


            const Unicode& uintVec = nodeInfo.word;
            TrieNode* p = _root;
            for(uint i = 0; i < uintVec.size(); i++)
            {
                uint16_t cu = uintVec[i];
                if(NULL == p)
                {
                    return false;
                }
                //if(p->hmap.end() == p->hmap.find(cu))
                TrieNode tmp; tmp.what = cu;
                if (!canfind(p->hmap.begin(), p->hmap.end(), tmp))
                {
                    TrieNode * next = NULL;
                    try
                    {
                        next = new TrieNode;
                    }
                    catch(const bad_alloc& e)
                    {
                        return false;
                    }
                    //p->hmap[cu] = next;
                    p->hmap.insert(lower_bound(p->hmap.begin(), p->hmap.end(), tmp), *next); //TODO
                    p = next;
                }
                else
                {
                    //p = p->hmap[cu];
                    p = &(*lower_bound(p->hmap.begin(), p->hmap.end(), tmp));
                }
            }
            if(NULL == p)
            {
                return false;
            }
            if(p->isLeaf)
            //if (p->nodeInfoVecPos >= 0)
            {
                LogError("this node already _inserted");
                return false;
            }

            p->isLeaf = true;
            _nodeInfoVec.push_back(nodeInfo);
            p->nodeInfoVecPos = _nodeInfoVec.size() - 1;

            return true;
        }

    private:
        bool _trieInsert(const char * const filePath)
        {
            ifstream ifs(filePath);
            if(!ifs)
            {
                LogError("open %s failed.", filePath);
                return false;
            }
            string line;
            vector<string> vecBuf;

            TrieNodeInfo nodeInfo;
            size_t lineno = 0;
            while(getline(ifs, line))
            {
                vecBuf.clear();
                lineno ++;
                split(line, vecBuf, " ");
                if (2 > vecBuf.size()) {
                    LogError("<2");
                    continue;
                }
                if(4 < vecBuf.size())
                {
                    LogError("line[%u:%s] illegal.", lineno, line.c_str());
                    continue;
                }
                if(!TransCode::decode(vecBuf[0], nodeInfo.word))
                {
                    LogError("line[%u:%s] illegal.", lineno, line.c_str());
                    continue;
                    //return false;
                }
                nodeInfo.freq = atoi(vecBuf[1].c_str());
                if(3 <= vecBuf.size())
                {
                    nodeInfo.tag = vecBuf[2];
                }
                if (4 <= vecBuf.size()) {
                    nodeInfo.logInfoGains = log(atof(vecBuf[3].c_str()));
                    _minLogInfoGains = min(nodeInfo.logInfoGains, _minLogInfoGains);
                }
                //LogError(line.c_str());
                //_insert node
                if(!_insert(nodeInfo))
                {
                    LogError("_insert node failed!");
                }
            }
            return true;
        }
        bool _countWeight()
        {
            if(_nodeInfoVec.empty() || 0 != _freqSum)
            {
                LogError("_nodeInfoVec is empty or _freqSum has been counted already.");
                return false;
            }

            //freq total freq
            for(size_t i = 0; i < _nodeInfoVec.size(); i++)
            {
                _freqSum += _nodeInfoVec[i].freq;
            }

            if(0 == _freqSum)
            {
                LogError("_freqSum == 0 .");
                return false;
            }

            //normalize
            for(uint i = 0; i < _nodeInfoVec.size(); i++)
            {
                TrieNodeInfo& nodeInfo = _nodeInfoVec[i];
                if(0 == nodeInfo.freq)
                {
                    LogFatal("nodeInfo.freq == 0!");
                    return false;
                }
                nodeInfo.logFreq = log(double(nodeInfo.freq)/double(_freqSum));
                if(_minLogFreq > nodeInfo.logFreq)
                {
                    _minLogFreq = nodeInfo.logFreq;
                }
            }

            return true;
        }

        bool _deleteNode(TrieNode* node)
        {
            /*
            for(TrieNodeMap::iterator it = node->hmap.begin(); it != node->hmap.end(); it++)
            {
                //TrieNode* next = it->second;
                _deleteNode(*it);
            }*/

            delete node;
            return true;
        }

    };
}

#endif
