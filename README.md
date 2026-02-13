The idea for this utility is to provide a way to generate simple QR codes that do not require a new account, a login, or the sharing of information with others, such as WiFi passwords. 
I have tinkered with this multiple times, but didn't finish. For this version, I used Android Studio (with Gemini for parts) and OpenCode from OpenCode.ai. I gave OpenCode the requirements.
It builds the code base. Then I spent time correcting the errors.
It was built to not require an account, login, or email info. I don't want your stuff.

Recommendations to readers.
1) Use OpenCode to generate the application.
2) Specify the Android, JDK, and API versions you want it to build for. I had an issue because it built the initial code for Gradle 8.x, not 9.x.
3) Think about graphical assets, icons you want it to have.

I haven't gone through the process to publish this on the Play Store yet. Don't know if it will work.
