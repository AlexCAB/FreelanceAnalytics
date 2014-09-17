package excavators.odesk

import java.util.Date


object Logger {
  //Methods
  def log(msg:String) = {
    //Date
    val d = new Date
    //Add to DB
                                       //<<<??????
                                       //Нежен потоко безопасный код
    //Add to UI
    UI.addLogMsg(d, msg)}}
