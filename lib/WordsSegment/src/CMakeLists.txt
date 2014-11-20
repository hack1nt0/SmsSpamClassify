cmake_minimum_required(VERSION 2.8.4)
project(src)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")

set(SOURCE_FILES
    jieba/Husky/HttpReqInfo.hpp
    jieba/Husky/HuskyServer.hpp
    jieba/Husky/ThreadManager.hpp
    jieba/Limonp/ArgvContext.hpp
    jieba/Limonp/cast_functs.hpp
    jieba/Limonp/Config.hpp
    jieba/Limonp/InitOnOff.hpp
    jieba/Limonp/io_functs.hpp
    jieba/Limonp/logger.hpp
    jieba/Limonp/macro_def.hpp
    jieba/Limonp/map_functs.hpp
    jieba/Limonp/md5.hpp
    jieba/Limonp/MysqlClient.hpp
    jieba/Limonp/std_outbound.hpp
    jieba/Limonp/str_functs.hpp
    jieba/FullSegment.hpp
    jieba/HMMSegment.hpp
    jieba/ISegment.hpp
    jieba/KeywordExtractor.hpp
    jieba/MIGPSegment.hpp
    jieba/Mix_MIGP_HMM_Segment.hpp
    jieba/MixSegment.hpp
    jieba/MPSegment.hpp
    jieba/QuerySegment.hpp
    jieba/segment.cpp
    jieba/SegmentBase.hpp
    jieba/TransCode.hpp
    jieba/Trie.hpp
    jieba/TrieManager.hpp
    )

add_executable(src ${SOURCE_FILES})