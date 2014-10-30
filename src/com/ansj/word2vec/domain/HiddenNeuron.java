package com.ansj.word2vec.domain;

public class HiddenNeuron extends Neuron{
    
    public double[] syn1 ; //hidden->modelOut
    
    public HiddenNeuron(int layerSize){
        syn1 = new double[layerSize] ;
    }
    
}
