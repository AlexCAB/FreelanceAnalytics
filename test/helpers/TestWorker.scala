package helpers

import javax.swing.{ImageIcon, JLabel, JDialog}
import excavators.odesk.ui.{Browser, ManagedWorker}

/**
 * Test ManagedWorker implementation
 * Created by WORK on 21.09.14.
 */

class TestWorker(b:Browser) extends ManagedWorker {
  private var w = false
  def goToMain() = {b.openURL("http://www.google.com")}
  def saveHtml() = {
    //println(b.getCurrentHTML)
    b.openURL("https://www.odesk.com/users/Ivan_N_~015d8df4348d317d56")}
  def saveScreenshot() = {
//    val img = b.captureImage
//    val d = new JDialog
//    d.setTitle("Test screenshot")
//    d.getContentPane.add(new JLabel(new ImageIcon(img)));
//    d.pack()
//    d.setVisible(true)
    println(b.getJSONByUrl("/job-description/MCvz%2FBa3TC50m2eYSC0ycHN0bzOgPtAcc5EQJcp%2BaE0%3D"))}
  def setPaused(s:Boolean) = {println("TestWorker.setWork = " + s); w = s}
  def isPaused:Boolean = w}
//
//Key	Value
//Request	GET /job-description/MCvz%2FBa3TC50m2eYSC0ycHN0bzOgPtAcc5EQJcp%2BaE0%3D HTTP/1.1