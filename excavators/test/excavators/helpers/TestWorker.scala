package excavators.helpers

import javax.swing.{ImageIcon, JLabel, JDialog}
import excavators.odesk.ui.{Browser, ManagedWorker}

/**
 * Test ManagedWorker implementation
 * Created by WORK on 21.09.14.
 */

class TestWorker(b:Browser) extends ManagedWorker {
  private var w = false
  def goToMain() = {b.openURL("http://www.google.com")}
  def saveHtml() = {println(b.getCurrentHTML)}
  def saveScreenshot() = {
    val img = b.captureImage(0,0,200,200)
    val d = new JDialog
    d.setTitle("Test screenshot")
    d.getContentPane.add(new JLabel(new ImageIcon(img)));
    d.pack()
    d.setVisible(true)}
  def setWork(s:Boolean) = {println("TestWorker.setWork = " + s); w = s}
  def isWork:Boolean = w}
