package org.fedoraproject.mobile

import Implicits._
import util.Hashing

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast

import scala.concurrent.{ future, Future }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Try, Success }

import scalaz.effect.IO

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher

class UserActivity
  extends NavDrawerActivity
  //with PullToRefreshAttacher.OnRefreshListener
  with util.Views {

  private lazy val refreshAdapter = new PullToRefreshAttacher(this)

  // The nickname is passed via the intent. If it's not there, something went
  // horribly wrong.
  private lazy val username: Option[String] =
    Option(getIntent.getExtras.getString("username"))


  private def showDemoWarning(): IO[Unit] = IO {
    val builder = new AlertDialog.Builder(this)
    builder.setNegativeButton("Right-o!", new DialogInterface.OnClickListener() {
      def onClick(dialog: DialogInterface, id: Int): Unit = {
      }
    })
    builder.setTitle("Hey there!")
    builder.setMessage("This is just a UI demo. The data is fake and is for UI testing only.")
    val dialog = builder.create
    dialog.show
  }

  override def onPostCreate(bundle: Bundle) {
    super.onPostCreate(bundle)
    setUpNav(R.layout.user_activity)

    showDemoWarning.unsafePerformIO

    val actionbar = getActionBar
    actionbar.setTitle(R.string.user_profile)

    // We don't have FAS integration yet (need OAuth) so all we can do is dream.
    // ...and fill in fake data to play with UI ideas.
    findView(TR.full_name).setText("Ricky Elrod")
    findView(TR.username).setText("codeblock")

    val profilePic = findView(TR.profile_pic)

    val image: Future[Bitmap] =
      Cache.getGravatar(
        this,
        Hashing.md5(s"codeblock@fedoraproject.org").toString)

    // XXX: Move this to scalaz Promise.
    image.onComplete {
      case Success(img) => runOnUiThread(profilePic.setImageBitmap(img))
      case _ => Log.e("UserActivity", "Unable to fetch profile image.")
    }

    findView(TR.badge_count).setText("43")
    findView(TR.fas_groups_count).setText("24")
    findView(TR.packages_count).setText("28")
  }
}
