const { onValueWritten } = require("firebase-functions/v2/database");
const admin = require("firebase-admin");

admin.initializeApp();

const db = admin.database();

exports.notifyOnLike = onValueWritten(
  "posts/{userId}/{date}/likedBy/{likerUid}",
  async (event) => {
    if (!event.data.after.exists()) return;

    const userId = event.params.userId;
    const likerUid = event.params.likerUid;

    const currentUserSnap = await db.ref(`users/${likerUid}/username`).once("value");
    const likerUsername = currentUserSnap.val() || "Someone";

    const tokenSnap = await db.ref(`users/${userId}/fcmToken`).once("value");
    const token = tokenSnap.val();

    if (!token) return;
    if (likerUid === userId) return;

    return admin.messaging().send({
      notification: {
        title: "New like!",
        body: `${likerUsername} liked your photo`,
      },
      token: token,
    });
  }
);

exports.notifyOnComment = onValueWritten(
  "posts/{userId}/{date}/comments",
  async (event) => {
    const userId = event.params.userId;
    const comments = event.data.after.val() || {};

    const latestComment = Object.values(comments)[Object.keys(comments).length - 1];
    if (!latestComment) return;

    const commenterUid = latestComment.userId;

    const commenterSnap = await db.ref(`users/${commenterUid}/username`).once("value");
    const commenterUsername = commenterSnap.val() || "Someone";

    const tokenSnap = await db.ref(`users/${userId}/fcmToken`).once("value");
    const token = tokenSnap.val();

    if (!token) return;

    return admin.messaging().send({
      notification: {
        title: "New comment!",
        body: `${commenterUsername} commented: ${latestComment.text?.substring(0, 50)}`,
      },
      token: token,
    });
  }
);