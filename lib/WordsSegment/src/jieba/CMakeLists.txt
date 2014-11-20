cmake_minimum_required(VERSION 2.8.4)
project(jieba)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")

set(SOURCE_FILES
    Husky/HttpReqInfo.hpp
    Husky/HuskyServer.hpp
    Husky/ThreadManager.hpp
    Limonp/ArgvContext.hpp
    Limonp/cast_functs.hpp
    Limonp/Config.hpp
    Limonp/InitOnOff.hpp
    Limonp/io_functs.hpp
    Limonp/logger.hpp
    Limonp/macro_def.hpp
    Limonp/map_functs.hpp
    Limonp/md5.hpp
    Limonp/MysqlClient.hpp
    Limonp/std_outbound.hpp
    Limonp/str_functs.hpp
    FullSegment.hpp
    HMMSegment.hpp
    ISegment.hpp
    KeywordExtractor.hpp
    MIGPSegment.hpp
    Mix_MIGP_HMM_Segment.hpp
    MixSegment.hpp
    MPSegment.hpp
    QuerySegment.hpp
    segment.cpp
    SegmentBase.hpp
    TransCode.hpp
    Trie.hpp
    TrieManager.hpp
    dict/hmm_model.utf8
    dict/jieba.dict.utf8
    test/testdata/testlines.utf8
    )

add_executable(jieba ${SOURCE_FILES})