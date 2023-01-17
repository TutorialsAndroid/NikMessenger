#Download APK [[Click here to download app]](https://mega.nz/file/8plnFRJR#pZTpilb1yiIuUVis9gO_WlCCU9-zfQkHJC8W36qyyIM)


# Nik Messenger

Nik is an secured messenger built using firebase realtime database.

## Features

- Doesn't require real phone number. It has its own virtual number generator.
- Any type of file sharing
- Delete chat from both sides
- Gif sends
- More Features are coming soon


## How it works
- App is backend by firebase realtime database database.

### 1. User Registration
- The first step when user create account it generates a virtual number randomly and the generated number is cross-checked weather the number was generated perviously or not. If number was not generated then it will continue to create user profile.
- The second step it ask user to enter username after entering username it stores the information in firebase realtime database.
- Please note : For security reason once the virtual number generated it is not given to any one else. It's unique for you. And if you accidentally delete the app then you will lost all chats and data. You can't logic back again.
### 2. Adding Contacts
- The process of adding contacts is very simple.
- First the person has to enter the virtual number of that person whom he wants to add.
- Second the app will check in database weather the entered virtual number exits in database or not. If it exits then the person data will be fetched and the contact will be added for that person.
- In this process of adding contact when a person adds a contact of another person. It generates a random chat room ID. Which will be useful for showing notifications and for one-to-one chat.
### 3. Showing Notifications
- For showing notifications it uses firebase messaging service.
- For one to one chats the person is subscribe to chat room id. Now whenever person sends a message to another person it will show a notification to person because the person is already subscribed to the chat room ID.
### 4. File Sharing
- App uploads the file in firebase cloud storage. Then it retrives the uploaded file url and sends it chat feed. So whenever a person access to the file the url is fetched and then the file gets download and viewed. And once the person leaves the chat the downloaded file automatically gets deleted.
