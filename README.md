#Download APK [[Click here to download app]](https://doc-0k-9k-docs.googleusercontent.com/docs/securesc/9kn8rtdevm1l3jvnco4evr5cgbhtpnp2/ubfoj6vt6ugpkkh581ejeq3eufbkj9q6/1673875725000/12696079304897399225/12696079304897399225/1MydSs4DLLCh4_H2jnFGvc_sQaElzSYoO?e=download&ax=ALjR8sxszybm-HMh24JDnCMPNDen64_PLT2z8YIliQDPdVYYAPUeYXA2b5EkNBLHwl-hotBwn28B7JTwBNEl7MEv3RU8uoCSxWYsGvjz-u8WqTc3Na33mNdaD_DenPX8Gdo1qEp3wl0kpUar5Cwp4CQUs6z4UcG23feaxraKyNvvm4HxSkhhYz1uQNHAJfXv5UENOv8LfLFgpFxFEcwwdGkDVY7qm7TymUBYT_QY4Hr_zqAw0EYHttM1KOvqUXgS5J_1yQ_Gcxw0auEU8CM88M9sDNEsqxnBbE4vQiGZoduJ6FjnkHC_7iTA57r9rZ7hLJ-LZGifVfnwWT5ITr-twlfF9cfGsgfifvqXc1lrWH__cHp5aUL6M8LT1-RltzbytiDJQ4VI-VE3LBJc8vsIhgZntcoCoOTCT61ZLv2KyTYIB0qGqyr-V7jD8DG9nXOSZNUa0ZE0W8tHlhPBcG9DqTDZLcmXliiG2Sm7Z71hXPlvfCd8YKY33VeGa4S6h6kz_YKLOQxDz_fIoUWEGPjDt5-VP5G0HF-hkyiypat4cj23XdrjEnOEY-x0PBhW_4R7cIZ80nE9usPxm_CXHVY7XpluFiVKwm-FFXO6VfW7thRe8w-XbCG8eDXTZ76TW85k2ep9uM3MQ4FTkhrgbMrxTgqyk8nGa3XEZNukmjry2UR4-Dkoz-JXqsAAF4dTXgsY4BgYcWjEYkRP90b9hKpBsh_6xDTOt0I8TjkJVaCJotqXlxNxMSRtmDEkUrJBi9_oDRHwo_lySE-hiEL9cYpS_ef5HE_RDpbllPLrV4Kgg1oJOnPKocmuCVxRuT55oio4r0Q_ivnmQ8QKAcorLgth42FzHLwArGSJaU7Rr4WD0IiWbb4x_TfqNxJ00CwfFdWrtTrlYVhbGesMG4osYCb58zXymCGH9z9DN8vrrrc&uuid=3738248d-6632-45c0-9eea-dee744c92d28&authuser=1&nonce=8uup9o8e2909u&user=12696079304897399225&hash=lphhfdok4n6pql0odjg960s8uijc1ij6)


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
