package com.example.myapplication

fun main(){
    primeNumber(7)
}

fun primeNumber(number : Int){
    for (i in 2 until 10){
        for (j in 2..i){
            if (i % j == 0){
                println("Not Prime Number $i")
            }
        }
    }
}