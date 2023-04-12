const functions = require('firebase-functions');
const admin = require('firebase-admin');


admin.initializeApp()

//message types
const SENT_IMAGE = 2;
const SENT_VIDEO = 5;
const SENT_VOICE_MESSAGE = 11;
const SENT_AUDIO = 9;
const SENT_FILE = 13;
const SENT_CONTACT = 16;
const SENT_LOCATION = 18;

//group events types
const ADMIN_ADDED = 1;
const USER_ADDED = 2;
const USER_REMOVED_BY_ADMIN = 3;
const USER_LEFT_GROUP = 4;
const GROUP_SETTINGS_CHANGED = 5;
const GROUP_CREATION = 6;
const ADMIN_REMOVED = 7;
const JOINED_VIA_LINK = 8;


const MESSAGE_TIME_LIMIT = 15;

const options = {
  priority: 'high'
}


//this function will called whenver a message is created at the node 'meesages' 
//it will get the message data including the sender and receiver ids 
//and lastly send it to the receiver using his registrationTokens
exports.sendMessageNotification = functions.database.ref('/messages/{messageId}').onCreate((snap, context) => {
  //get the message object
  const val = snap.val();
  //get fromId field
  const fromId = val.fromId;
  //get toId field
  const toId = val.toId;
  //get messageId
  const messageId = context.params.messageId;

  //message Details
  const content = val.content;
  const metadata = val.metadata;
  const timestamp = val.timestamp;
  const type = val.type;

  console.log("MessageId", messageId);
  console.log("friendId", toId);
  console.log("Content ", content)





  // Get the list of device notification tokens.
  const getDeviceTokensPromise = admin.database().ref(`users/${toId}/notificationTokens/`).once('value');
  //get user info
  const getSenderInfo = admin.database().ref(`users/${fromId}/phone`).once('value');

  //determine if user is blocked
  const isUserBlocked = admin.database().ref(`blockedUsers/${toId}/${fromId}/`).once('value');

  //Execute the Functions
  return Promise.all([getDeviceTokensPromise, getSenderInfo, isUserBlocked]).then(results => {
    const tokensSnapshot = results[0];
    const friendSnapshot = results[1];
    const isBlockedSnapshot = results[2];



    // Check if there are notification tokens tokens.
    if (!tokensSnapshot.hasChildren()) {
      return console.log('There are no notification tokens to send to.');
    }

    //check if user is blocked,if so do not send the message to him
    if (isBlockedSnapshot.exists()) {
      return console.log('user is blocked !');
    }

    //get sender phone number
    const senderPhone = friendSnapshot.val();
    console.log("sender phone is " + senderPhone);


    //payload contains the data to send it to receiver
    var payload = getMessagePayload(type, val, senderPhone, content, timestamp, fromId, toId, undefined, messageId, metadata);


    // Listing all tokens.
    const tokens = Object.keys(tokensSnapshot.val());

    // Send notifications to all tokens.
    return admin.messaging().sendToDevice(tokens, payload, options).then(response => {
      console.log("Sending notification...");

      // For each message check if there was an error.
      const tokensToRemove = [];
      response.results.forEach((result, index) => {
        const error = result.error;
        if (error) {
          console.error('Failure sending notification to', tokens[index], error);
          // Cleanup the tokens who are not registered anymore.
          if (error.code === 'messaging/invalid-registration-token' ||
            error.code === 'messaging/registration-token-not-registered') {
            tokensToRemove.push(tokensSnapshot.ref.child(tokens[index]).remove());
          }
        }
      });
      return Promise.all(tokensToRemove).catch((err => {
        console.log("error happen when sending messages with error " + err)
      }));
    });
  });
});

//this function will send notifications whenever a new message created for a certain group
exports.sendMessagesForGroups = functions.database.ref(`/groupsMessages/{groupId}/{messageId}`).onCreate((snap, context) => {
  const groupId = context.params.groupId
  //get messageId
  const messageId = context.params.messageId
  const val = snap.val()
  const fromPhone = val.fromPhone
  const fromId = val.fromId;

  //message Details
  const content = val.content;
  const metadata = val.metadata;
  const timestamp = val.timestamp;
  const type = val.type;

  const payload = getMessagePayload(type, val, fromPhone, content, timestamp, fromId, groupId, true, messageId, metadata);
  //send message to the givern groupId
  return admin.messaging().sendToTopic(groupId, payload, options)

})

//this will trigger whenever a new user added to a group
exports.participantAdded = functions.database.ref(`/groups/{groupId}/users/{userId}`).onCreate((snap, context) => {
  const groupId = context.params.groupId
  //the ID of the added user
  const userId = context.params.userId
  //the ID of the admin who added the user
  const addedByUid = context.auth.uid


  //this boolean will determine if the id was the same id of admin id
  const isThisUserCreatedTheGroup = addedByUid === userId && snap.val();

  console.log("adding participant ", userId)

  //get userPhone
  const userPhonePromise = admin.database().ref(`/users/${userId}/phone`).once('value')
  //get admin who added the user phone's 
  const addedByPhonePromise = admin.database().ref(`/users/${addedByUid}/phone`).once('value')

  //execute the above Promises
  return Promise.all([userPhonePromise, addedByPhonePromise]).then((results) => {
    const userPhone = results[0].val()
    const addedByPhone = results[1].val()

    var eventType;
    if (userPhone === addedByPhone) {
      //if it's true then it's an Admin and it's the Group creator
      if (snap.val())
        eventType = GROUP_CREATION
      else
        //if it's false then it's a member that joined using an link
        eventType = JOINED_VIA_LINK
    }
    else {
      eventType = USER_ADDED
    }

    const time = Date.now()
    console.log('user phone is ', userPhone)
    console.log('added by phone is ', addedByPhone)
    const event = {
      contextStart: `${addedByPhone}`,
      eventType: eventType,
      contextEnd: `${eventType === JOINED_VIA_LINK ? "null" : userPhone}`,
      timestamp: `${time}`
    }

    //if it's JOINED_VIA_LINK then set it as "link" else set it as the person who added this user
    var addedBy = eventType === JOINED_VIA_LINK ? "link" : addedByPhone;
    //set group event
    return admin.database().ref(`/groupEvents/${groupId}`).push().set(event).then(() => {
      //set who added this user to this group
      return admin.database().ref(`/groupMemberAddedBy/${userId}/${groupId}`).set(addedBy).then(() => {
        //set groupId for this user to determine what groups are participated in
        return admin.database().ref(`/groupsByUser/${userId}/${groupId}`).set(isThisUserCreatedTheGroup).then(() => {
          //delete the deleted user previously so he can join the group via invitaton link
          //also this is called only when an admin adds this user
          return admin.database().ref(`/groupsDeletedUsers/${groupId}/${userId}`).remove()
        })
      })
    })

  })



})

//this group events will trigger whenever a new change happened to a group, user added,admin removed, etc..
exports.groupEvents = functions.database.ref(`/groupEvents/{groupId}/{eventId}`).onCreate((snap, context) => {
  const groupId = context.params.groupId
  const eventId = context.params.eventId
  const val = snap.val()

  const contextStart = val.contextStart;
  const eventType = val.eventType.toString();
  const contextEnd = val.contextEnd;

  const payload = {
    data: {
      event: 'group_event',
      groupId: `${groupId}`,
      eventId: `${eventId}`,
      contextStart: `${contextStart}`,
      eventType: eventType,
      contextEnd: `${contextEnd}`
    }
  }


  return admin.messaging().sendToTopic(groupId, payload)
})



//this will send a message to the user to make him fetch the group data including the users in this a group and subscribe to FCM topic
exports.addUserToGroup = functions.database.ref(`/groupsByUser/{userId}/{groupId}`).onCreate((snap, context) => {
  const userId = context.params.userId
  const groupId = context.params.groupId
  //this boolean will determine if there is a need to send a notification
  //if it's false we don't need to send the message, since the user itself has created this group
  //and he already subscribed and fetched user data
  const sendNotification = !snap.val()

  const payload = {
    data: {
      event: 'new_group',
      groupId: `${groupId}`
    }
  }


  if (!sendNotification) {
    return null
  }
  console.log('add user to group event', groupId)

  return admin.database().ref(`users/${userId}/notificationTokens/`).once('value').then((snapshot) => {
    const tokens = Object.keys(snapshot.val());

    // Send notifications to all tokens.
    return admin.messaging().sendToDevice(tokens, payload, options).then(response => {
      console.log("Sending notification...");

      // For each message check if there was an error.
      const tokensToRemove = [];
      response.results.forEach((result, index) => {
        const error = result.error;
        if (error) {
          console.error('Failure sending notification to', tokens[index], error);
          // Cleanup the tokens who are not registered anymore.
          if (error.code === 'messaging/invalid-registration-token' ||
            error.code === 'messaging/registration-token-not-registered') {
            tokensToRemove.push(snapshot.ref.child(tokens[index]).remove());
          }
        }
      });
      return Promise.all(tokensToRemove).catch((err => {
        console.log("error happen when sending messages with error " + err)
      }));
    });
  })

})

//this will unsubscribe the user from FCM Topic (Group) when he removed from the group
exports.unsubscribeUserFromTopicOnDelete = functions.database.ref(`/groupsByUser/{userId}/{groupId}`).onDelete((snap, context) => {
  const userId = context.params.userId
  const groupId = context.params.groupId

  console.log('unsubscribing from topic ', groupId + " for user " + userId)


  return admin.database().ref(`users/${userId}/notificationTokens/`).once('value').then((snapshot) => {
    const tokens = Object.keys(snapshot.val());

    return admin.messaging().unsubscribeFromTopic(tokens, groupId)


  })

})

//this will trigger when a group member removed
exports.participantRemoved = functions.database.ref(`/groups/{groupId}/users/{userId}`).onDelete((snap, context) => {
  const groupId = context.params.groupId
  const userId = context.params.userId
  const deletedByUid = context.auth.uid



  //get removed user phone
  const userPhonePromise = admin.database().ref(`/users/${userId}/phone`).once('value')
  //get phone of the admin who removed this user
  const removedByPhonePromise = admin.database().ref(`/users/${deletedByUid}/phone`).once('value')

  //execute above promises
  return Promise.all([userPhonePromise, removedByPhonePromise]).then((results) => {
    const userPhone = results[0].val()
    const removedByPhone = results[1].val()
    const time = Date.now()
    console.log('user phone is ', userPhone)
    console.log('added by phone is ', removedByPhone)

    var contextStart;
    var contextEnd;
    var eventType;
    //if the id is the same id of deletedById,then user exits the group by himself
    if (userId === deletedByUid) {
      eventType = USER_LEFT_GROUP
      contextStart = userPhone
      contextEnd = 'null'
    }
    //otherwise an admin removed this user
    else {
      eventType = USER_REMOVED_BY_ADMIN
      contextStart = removedByPhone
      contextEnd = userPhone
    }

    const event = {
      contextStart: `${contextStart}`,
      eventType: eventType,
      contextEnd: `${contextEnd}`,
      timestamp: `${time}`
    }

    //if user removed by admin then add his id to deleted users of the group
    //this will prevent this user from joining the group again when using Group Invitation Link


    if (eventType === USER_REMOVED_BY_ADMIN) {
      console.log('user removed by admin')
      return admin.database().ref(`/groupsDeletedUsers/${groupId}/${userId}`).set(true).then(() => {
      console.log(`setting user ${userId} for groupId ${groupId} as deleted` )

        //set group Event in database
        return admin.database().ref(`/groupEvents/${groupId}`).push().set(event).then(() => {


          //if the deleted user is an admin, then check if there are other admins,if not set a new admin randomly
          if (snap.val() === true) {
            return admin.database().ref(`/groups/${groupId}/users/`).once('value').then((snapshot) => {



              //if the group is not exists return null and do nothing
              if (snapshot.val() === null) {
                return null
              }

              const users = snapshot.val()
              console.log('users ', JSON.stringify(users))
              //check if there is another admin,if not set a new admin randomly


              //check if there is another admin in group, if not we will generate an admin randomly
              if (!isThereAdmin(users)) {



                //get current users
                const usersArray = Object.keys(snapshot.val())

                //generate a new admin
                const newAdminUid = usersArray[Math.floor(Math.random() * usersArray.length)];



                console.log("newAdminUid is ", newAdminUid)
                //set new admin
                return admin.database().ref(`/groups/${groupId}/users/${newAdminUid}`).set(true).then(() => {
                  console.log('setting a new admin ', newAdminUid)
                  //remove the user from group 
                  return admin.database().ref(`/groupsByUser/${userId}/${groupId}`).remove().then(() => {
                    // return admin.messaging().sendToTopic(groupId, payload).then(() => {
                    //   console.log("done sending participant removed event")
                    // })
                  })
                })
              }
            })
          }



          //if the removed user is not admin ,just remove him from the group
          return admin.database().ref(`/groupsByUser/${userId}/${groupId}`).remove()

        })

      })
    }

    //set group Event in database
    return admin.database().ref(`/groupEvents/${groupId}`).push().set(event).then(() => {


      //if the deleted user is an admin, then check if there are other admins,if not set a new admin randomly
      if (snap.val() === true) {
        return admin.database().ref(`/groups/${groupId}/users/`).once('value').then((snapshot) => {



          //if the group is not exists return null and do nothing
          if (snapshot.val() === null) {
            return null
          }

          const users = snapshot.val()
          console.log('users ', JSON.stringify(users))
          //check if there is another admin,if not set a new admin randomly


          //check if there is another admin in group, if not we will generate an admin randomly
          if (!isThereAdmin(users)) {



            //get current users
            const usersArray = Object.keys(snapshot.val())

            //generate a new admin
            const newAdminUid = usersArray[Math.floor(Math.random() * usersArray.length)];



            console.log("newAdminUid is ", newAdminUid)
            //set new admin
            return admin.database().ref(`/groups/${groupId}/users/${newAdminUid}`).set(true).then(() => {
              console.log('setting a new admin ', newAdminUid)
              //remove the user from group 
              return admin.database().ref(`/groupsByUser/${userId}/${groupId}`).remove().then(() => {
                // return admin.messaging().sendToTopic(groupId, payload).then(() => {
                //   console.log("done sending participant removed event")
                // })
              })
            })
          }
        })
      }



      //if the removed user is not admin ,just remove him from the group
      return admin.database().ref(`/groupsByUser/${userId}/${groupId}`).remove()

    })


  })
})

//this will called when an admin changed (removed,or added)
exports.groupAdminChanged = functions.database.ref(`/groups/{groupId}/users/{userId}`).onUpdate((change, context) => {
  const groupId = context.params.groupId
  const userId = context.params.userId
  var addedById = undefined

  //check if the admin was added by another admin or has ben set Randomly by cloud functions
  if (context.auth !== undefined) {
    addedById = context.auth.uid
  }


  console.log('addedById ', addedById)
  //check if admin added or removed
  const isNowAdmin = change.after.val()

  const userPhonePromise = admin.database().ref(`/users/${userId}/phone`).once('value')
  const addedByPhonePromise = admin.database().ref(`/users/${addedById}/phone`).once('value')

  //if there is no admin left in group ,then it will be set by functions,therefore 'addedById' will be undefined
  if (addedById === undefined) {
    return userPhonePromise.then((snap) => {
      const userPhone = snap.val()
      const timestamp = Date.now()
      const event = {
        contextStart: `null`,
        eventType: eventType,
        contextEnd: `${userPhone}`,
        timestamp: `${timestamp}`
      }

      console.log('group admin change event ', "groupId " + groupId + " userId " + userId)

      return admin.database().ref(`/groupEvents/${groupId}/`).push().set(event)
    })
  }

  //otherwise get users phone and set the event
  return Promise.all([userPhonePromise, addedByPhonePromise]).then((results) => {
    const userPhone = results[0].val()
    const addedByPhone = results[1].val()
    var eventType;

    if (isNowAdmin) {
      eventType = ADMIN_ADDED
    }
    else {
      eventType = ADMIN_REMOVED
    }

    const timestamp = Date.now()
    const event = {
      contextStart: `${addedByPhone}`,
      eventType: eventType,
      contextEnd: `${userPhone}`,
      timestamp: `${timestamp}`
    }


    return admin.database().ref(`/groupEvents/${groupId}/`).push().set(event)

  })

})

//this will called when the photo or 'onlyAdminsCanPost' changed
exports.groupInfoChanged = functions.database.ref(`/groups/{groupId}/info`).onUpdate((change, context) => {
  const groupId = context.params.groupId
  const changedById = context.auth.uid


  return admin.database().ref(`/users/${changedById}/phone`).once('value').then((snapshot) => {
    const changedByPhone = snapshot.val()
    const event = {
      eventType: GROUP_SETTINGS_CHANGED,
      contextStart: `${changedByPhone}`,
      contextEnd: 'null'
    }

    return admin.database().ref(`/groupEvents/${groupId}/`).push().set(event)

  })



})

//this will delete the message for every one in the group
exports.deleteMessageForGroup = functions.database.ref(`deleteMessageRequestsForGroup/{groupId}/{messageId}`).onCreate((snap, context) => {
  const groupId = context.params.groupId
  const messageId = context.params.messageId

  //get the message
  return admin.database().ref(`groupsMessages/${groupId}/${messageId}`).once('value').then((results) => {
    const message = results.val()
    const timestamp = message.timestamp
    //check if message time has not passed 
    if (!timePassed(timestamp)) {
      //send delete message to the group
      return admin.messaging().sendToTopic(groupId, getDeleteMessagePayload(messageId), options)
    }
    return null

  })
})

//this will delete the message for every one in the group
exports.deleteMessageForBroadcast = functions.database.ref(`deleteMessageRequestsForBroadcast/{broadcastId}/{messageId}`).onCreate((snap, context) => {
  const broadcastId = context.params.broadcastId
  const messageId = context.params.messageId

  //get the message
  return admin.database().ref(`broadcastsMessages/${broadcastId}/${messageId}`).once('value').then((results) => {
    const message = results.val()
    const timestamp = message.timestamp
    //check if message time has not passed 
    if (!timePassed(timestamp)) {
      //send delete message to the group
      return admin.messaging().sendToTopic(broadcastId, getDeleteMessagePayload(messageId), options)
    }
    return null

  })
})

//this will delete message for other user in a chat
exports.deleteMessage = functions.database.ref(`/deleteMessageRequests/{messageId}`).onCreate((snap, context) => {
  const messageId = context.params.messageId

  const payload = getDeleteMessagePayload(messageId)



  return admin.database().ref(`messages/${messageId}`).once('value').then((results) => {
    const message = results.val()
    const timestamp = message.timestamp
    const toId = message.toId

    if (!timePassed(timestamp)) {


      return admin.database().ref(`/users/${toId}/notificationTokens`)
        .once('value')
        .then((tokensSnapshot) => {
          // Check if there are notification tokens.
          if (!tokensSnapshot.hasChildren()) {
            return console.log('There are no notification tokens to send to.');
          }

          const tokens = Object.keys(tokensSnapshot.val())


          return admin.messaging().sendToDevice(tokens, payload, options)

        })
    }

    return null

  })

})

//this function will trigger whenver a NEW User registered to this app
//it will get his uid and save it along with the phone number 
//this is used when we are trying to querying for a user by his phone
//so we can get his data from 'users' node by his uid later
exports.saveUidOnLogin = functions.auth.user().onCreate((userRecord, context) => {
  const uid = userRecord.uid
  const phoneNumber = userRecord.phoneNumber;
  return admin.database().ref(`uidByPhone/${phoneNumber}`).set(uid);
})

//get server time
exports.getTime = functions.https.onCall((data, context) => {
  return Date.now()
})


exports.setStatusCount = functions.database.ref(`statusSeenUids/{uid}/{statusId}`).onCreate((snap, context) => {
  const uid = context.params.uid
  const statusId = context.params.statusId

  return admin.database().ref(`statusCount/${uid}/${statusId}`).once('value').then((results => {
    const count = results.exists() ? snap.val() : 0
    return admin.database().ref(`statusCount/${uid}/${statusId}`).set(count + 1)
  }))
})

exports.subscribeToBroadcast = functions.database.ref(`broadcasts/{broadcastId}/users/{uid}`).onCreate((snap, context) => {
  const uid = context.params.uid
  const broadcastId = context.params.broadcastId
  const isCreator = snap.val()

  return admin.database().ref(`broadcastsByUser/${uid}/${broadcastId}`).set(isCreator).then(() => {
    //if it's true then this user is whom created the broadcast and there is no need to subscribe to topic
    if (snap.val()) {
      return null
    }

    return admin.database().ref(`users/${uid}/notificationTokens`).once('value').then((results) => {
      const tokens = Object.keys(results.val())
      //subscribe to FCM Topic
      return admin.messaging().subscribeToTopic(tokens, broadcastId)
    })

  })
})

//this is to resubscribe the user for broadcasts when he re-installs the app(when a new notification token generated)
exports.resubscribeUserToBroadcasts = functions.database.ref(`users/{uid}/notificationTokens/{token}`).onCreate((snap, context) => {
  const uid = context.params.uid
  const token = context.params.token
  return admin.database().ref(`broadcastsByUser/${uid}`).once('value').then((results) => {
    const promises = []
    results.forEach((snapshot) => {
      //add only the broadcasts that are not created by the user,since we don't need to subscribe him to broadcast
      if (!snapshot.val()) {
        promises.push(admin.messaging().subscribeToTopic(token, snapshot.key))
      }


    })

    return Promise.all(promises).then((results) => {
      console.log('resubscribed user ' + uid)
    })
  })
})

exports.sendMessageToBroadcast = functions.database.ref(`broadcastsMessages/{broadcastId}/{messageId}`).onCreate((snap, context) => {
  //get the message object
  const val = snap.val();
  //get fromId field
  const fromId = val.fromId;
  //get toId field
  const toId = val.toId;
  //get messageId
  const messageId = context.params.messageId;
  const broadcastId = context.params.broadcastId;

  //message Details
  const content = val.content;
  const metadata = val.metadata;
  const timestamp = val.timestamp;
  const type = val.type;

  console.log("MessageId", messageId);
  console.log("friendId", toId);
  console.log("Content ", content)






  //get user info
  const getSenderInfo = admin.database().ref(`users/${fromId}/phone`).once('value');

  //determine if user is blocked
  const isUserBlocked = admin.database().ref(`blockedUsers/${toId}/${fromId}/`).once('value');

  //Execute the Functions
  return Promise.all([getSenderInfo, isUserBlocked]).then(results => {
    const friendSnapshot = results[0];
    const isBlockedSnapshot = results[1];



    //check if user is blocked,if so do not send the message to him
    if (isBlockedSnapshot.exists()) {
      return console.log('user is blocked !');
    }

    //get sender phone number
    const senderPhone = friendSnapshot.val();
    console.log("sender phone is " + senderPhone);


    //payload contains the data to send it to receiver
    var payload = getMessagePayload(type, val, senderPhone, content, timestamp, fromId, toId, undefined, messageId, metadata);


    return admin.messaging().sendToTopic(broadcastId, payload)
  });
})

exports.unsubscribeUserFromBroadcast = functions.database.ref(`broadcasts/{broadcastId}/users/{userId}`).onDelete((snap, context) => {
  const userId = context.params.userId
  const broadcastId = context.params.broadcastId


  return admin.database().ref(`users/${userId}/notificationTokens/`).once('value').then((snapshot) => {
    const tokens = Object.keys(snapshot.val());

    return admin.database.ref(`broadcastsByUser/${userId}/${broadcastId}`).remove().then(() => {
      return admin.messaging().unsubscribeFromTopic(tokens, broadcastId)
    })


  })
})



function getDeleteMessagePayload(messageId) {
  return {
    data: {
      event: "message_deleted",
      messageId: `${messageId}`
    }
  };
}


function getMessagePayload(type, val, senderPhone, content, timestamp, fromId, toId, isGroup, messageId, metadata) {
  var payload;
  if (type == SENT_IMAGE || type == SENT_VIDEO) {
    //get blurred thumb from image or video
    const thumb = val.thumb;
    //get media duration if it's a Video if not it will send 'undefined'
    const mediaDuration = val.mediaDuration;
    // Notification details.
    payload = {
      data: {
        phone: `${senderPhone}`,
        content: `${content}`,
        timestamp: `${timestamp}`,
        fromId: `${fromId}`,
        toId: `${toId}`,
        messageId: `${messageId}`,
        type: `${type}`,
        metadata: `${metadata}`,
        thumb: `${thumb}`,
        mediaDuration: `${mediaDuration}`,
        isGroup: `${isGroup}`
      }
    };
  }
  else if (type == SENT_VOICE_MESSAGE || type == SENT_AUDIO) {
    //get voice message or audio duration
    const mediaDuration = val.mediaDuration;
    payload = {
      data: {
        phone: `${senderPhone}`,
        content: `${content}`,
        timestamp: `${timestamp}`,
        fromId: `${fromId}`,
        toId: `${toId}`,
        messageId: `${messageId}`,
        type: `${type}`,
        metadata: `${metadata}`,
        mediaDuration: `${mediaDuration}`,
        isGroup: `${isGroup}`
      }
    };
  }
  else if (type == SENT_FILE) {
    //get file size
    const fileSize = val.fileSize;
    payload = {
      data: {
        phone: `${senderPhone}`,
        content: `${content}`,
        timestamp: `${timestamp}`,
        fromId: `${fromId}`,
        toId: `${toId}`,
        messageId: `${messageId}`,
        type: `${type}`,
        metadata: `${metadata}`,
        fileSize: `${fileSize}`,
        isGroup: `${isGroup}`
      }
    };
  }
  else if (type == SENT_CONTACT) {
    //convert contact map to JSON to send it as JSON string to the client
    const contact = JSON.stringify(val.contact);
    payload = {
      data: {
        phone: `${senderPhone}`,
        content: `${content}`,
        timestamp: `${timestamp}`,
        fromId: `${fromId}`,
        toId: `${toId}`,
        messageId: `${messageId}`,
        type: `${type}`,
        metadata: `${metadata}`,
        contact: `${contact}`,
        isGroup: `${isGroup}`
      }
    };
  }
  else if (type == SENT_LOCATION) {
    //convert location map to JSON to send it as JSON string to the client      
    const location = JSON.stringify(val.location);
    payload = {
      data: {
        phone: `${senderPhone}`,
        content: `${content}`,
        timestamp: `${timestamp}`,
        fromId: `${fromId}`,
        toId: `${toId}`,
        messageId: `${messageId}`,
        type: `${type}`,
        metadata: `${metadata}`,
        location: `${location}`,
        isGroup: `${isGroup}`
      }
    };
  }
  //it's a Text Message
  else {
    payload = {
      data: {
        phone: `${senderPhone}`,
        content: `${content}`,
        timestamp: `${timestamp}`,
        fromId: `${fromId}`,
        toId: `${toId}`,
        messageId: `${messageId}`,
        type: `${type}`,
        metadata: `${metadata}`,
        isGroup: `${isGroup}`
      }
    };
  }

  const data = payload['data']
  data['quotedMessageId'] = val.quotedMessageId
  removeUndefined(data)
  return payload;

}

//remove undefined items from payload
function removeUndefined(obj) {
  for (var propName in obj) {
    if (typeof obj[propName] === "undefined" || obj[propName] === undefined || obj[propName] === 'undefined') {
      delete obj[propName];
    }
  }
}


//check if there is another admin in a group
function isThereAdmin(obj) {
  for (const k in obj) {
    if (obj[k] === true) {
      return true;
    }
  }
}

//check if message time not passed
function timePassed(timestamp) {
  return Math.floor((new Date() - timestamp) / 60000) > MESSAGE_TIME_LIMIT
  //todo convert 15 to global const
}

