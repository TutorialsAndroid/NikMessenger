
# Nik Messenger

Nik is an secured messenger built using firebase realtime database.

## Features

- Doesn't require real phone number. It has its own virtual number generator.
- Secure file sharing. When a person sends a file in chat the other person can't view the file until the owner of the file allows you to view.
- Delete chat from both sides
- Gif sends
- More Features are coming soon


## How it works
- App is backend by firebase realtime database database.

### 1. User Registration
- The first step when user create account it generates a virtual number randomly and the generated number is cross-checked weather the number was generated perviously or not. If number was not generated then it will continue to create user profile.
- The second step it ask user to enter username after entering username it stores the information in firebase realtime database.
### 2. Adding Contacts
- The process of adding contacts is very simple.
- First the person has to enter the virtual number of that person whom he wants to add.
- Second the app will check in database weather the entered virtual number exits in database or not. If it exits then the person data will be fetched and the contact will be added for that person.
- In this process of adding contact when a person adds a contact of another person. It generates a random chat room ID. Which will be useful for showing notifications and for one-to-one chat.
### 3. Showing Notifications
- For showing notifications it uses firebase messaging service.
- For one to one chats the person is subscribe to chat room id. Now whenever person sends a message to another person it will show a notification to person because the person is already subscribed to the chat room ID.
### 4. File Sharing
- App has a unique file sharing model. A person can share any type of file to the another person on the chat.
- When a person shares a file to another person by default it is protected. The another person will require the owner permission to view the file. Once the owner of the file allow to view the file the other person can successfully view the file.
- And the best part is that the file gets deleted automatically once the person leaves the chat.
