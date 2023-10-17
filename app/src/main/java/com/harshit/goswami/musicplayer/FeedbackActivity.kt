package com.harshit.goswami.musicplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.harshit.goswami.musicplayer.databinding.ActivityFeedbackBinding

class FeedbackActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFeedbackBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.navThemes[MainActivity.themeIndex])
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Feedback"


        binding.sendFA.setOnClickListener {
//                val feedbackMsg = binding.feedbackMsgFA.text.toString() + "\n" + binding.emailFA.text.toString()
//                val subject = binding.topicFA.text.toString()
//                val userName = "teamday38@gmail.com"
//                val pass = "TechnoDays_team"
//                val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//                if ((feedbackMsg.isNotEmpty() && subject.isNotEmpty() && cm.activeNetworkInfo?.isConnectedOrConnecting == true))
//                { Thread{
//                    try {
//                    val properties = Properties()
//                        properties["mail.smtp.auth"] = "true"
//                        properties["mail.smtp.starttls.enable"] = "true"
//                        properties["mail.smtp.host"] = "smtp.gmail.com"
//                        properties["mail.smtp.port"] = "587"
//
//                    val session = Session.getInstance(properties, object : Authenticator(){
//                        override fun getPasswordAuthentication(): PasswordAuthentication {
//                            return PasswordAuthentication(userName,pass)
//                        }
//                    })

//                    val mail = MimeMessage(session)
//                    mail.setFrom(InternetAddress(userName))
//                    mail.setRecipients(Message.RecipientType.TO,InternetAddress.parse("harshitharshit183@gmail.com"))
//                    mail.subject = subject
//                    mail.setText(feedbackMsg)
//
//                  send(mail)
//                }catch (e: MessagingException){ Toast.makeText(this,"1.$e",Toast.LENGTH_SHORT).show() }
//                }.start()
//                    try {
                        Toast.makeText(this,"Thanks for Feedback!!",Toast.LENGTH_SHORT).show()
                       finish()
//                    }catch (e:Exception){ Toast.makeText(this,"2.$e",Toast.LENGTH_SHORT).show() }
//
//                }
//                else
//                    Toast.makeText(this,"Something Went Wrong!!",Toast.LENGTH_SHORT).show()

        }

        }
    }