package com.msp.backHub.service

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.msp.backHub.Repo.TestTableRepo
import com.msp.backHub.controller.TestingController
import com.msp.backHub.entity.TestTable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class JpaTest {

    @Autowired
    private lateinit var  table: TestTableRepo


    fun jpaSaveTest(){
        val saveENt = TestTable()
        saveENt.id = Random.nextLong()
        saveENt.name = "RAGHU NA YAARU NU NANICHA DAAA"

      try{
          table.saveAndFlush(saveENt)

      } catch (ex:Exception){
          println("JPA SAVE FUCKED ${ex.stackTrace}")
      }
    }


    fun jsonSave(jsonString: String) {
        val jsonSabve = TestTable()
        jsonSabve.id = Random.nextLong()
        jsonSabve.name = "someName"
        jsonSabve.data = jsonString
        try{
            table.saveAndFlush(jsonSabve)
        } catch (ex:Exception){
            println("JPA JSON SAVE FUCKED ${ex.printStackTrace()}")
        }
    }






    fun jpaGetTEst():List<TestTable>{
       return table.findAll()
    }



    fun jsonGet(){
        val row = table.findById(2).get()

        val jsonData = row.data
        println("got jsonData $jsonData ")

       val js = Gson().fromJson(jsonData,TestingController.JsInput::class.java)

        println("jsoget : ${js.k2}")

    }




}