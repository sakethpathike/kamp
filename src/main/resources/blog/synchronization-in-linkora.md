---
title: Synchronization in Linkora
pubDatetime: Saturday 31-05-2025 12:52 PM IST 
---

[Linkora App](https://github.com/LinkoraApp/Linkora) uses multiple _techniques_ to make sure the data is synced with the
remote database even when the [self-hostable sync-server](https://github.com/LinkoraApp/sync-server) is
not up (i.e., Linkora will push changes once the server is up the next time). Most of the important parts of this
implementation happen in the app because it's the source of the data, so we'll have fine control over what's supposed to
be pushed and what's not.

Linkora supports `Two-Way Sync` synchronization. It's up to people who use the app to decide how to use the syncing
method.
Linkora supports:

- `Client To Server`
- `Server To Client`
- `Two-Way Sync`

Based on the selected option, Linkora will handle the respective implementations.

All this in a nutshell looks like:

```kotlin
suspend fun syncData() {
   if (canPushToServer()) {
      pushUnSyncedDataToServer()
   }

   if (canReadFromServer()) {
      establishSocketConnectionAndPerformOperations()

      getTombstonesInfoFromServer(after = TIME_STAMP).let {
         deleteFromLocalDataBasedOnTombstones(it)
      }

      getNewUpdatesFromServer(after = TIME_STAMP).let {
         updateLocalDataBasedOnRemoteUpdates(it)
      }
   }
}
```

By this, it is straightforward to understand that if the sync type is set to `Two-Way Sync`, both of these conditional
blocks will be true. Hence, we need to implement `Client-to-Server` and `Server-To-Client`.

### 1. Client-to-Server

In this case, we only need to consider:

1. Pushing `CREATE`-`UPDATE`-`DELETE` operations that happen locally.  
   That's all we care about. But there may be cases when the `sync-server` might not be up. In that case, we need to
   save what's supposed to be pushed so that whenever the server and app are up, the app can send those changes. This
   also makes it local-first, as irrespective of server changes, it will always update locally.

Now, the first thing is to *try saving locally and then pushing the changes*. There are many operations where we need
to push changes to the server, so I made a generic function that works for all these cases where we need to perform
local operations and then push to the remote server:

```kotlin
fun <LocalType, RemoteType> performLocalOperationWithRemoteSyncFlow(
   performRemoteOperation: Boolean,
   remoteOperation: suspend () -> Flow<Result<RemoteType>> = { emptyFlow() },
   remoteOperationOnSuccess: suspend (RemoteType) -> Unit = {},
   onRemoteOperationFailure: suspend () -> Unit = {},
   localOperation: suspend () -> LocalType
): Flow<Result<LocalType>> {
   return flow {
      emit(Result.Loading())
      val localResult = localOperation()
      Result.Success(localResult).let { success ->
         if (performRemoteOperation && canPushToServer()) {
            remoteOperation().collect { remoteResult ->
               remoteResult.onFailure { failureMessage ->
                  success.isRemoteExecutionSuccessful = false
                  success.remoteFailureMessage = failureMessage
                  onRemoteOperationFailure()
               }
               remoteResult.onSuccess {
                  remoteOperationOnSuccess(it.data)
               }
            }
         }
         emit(success)
      }
   }.catchAsThrowableAndEmitFailure(init = {
      if (performRemoteOperation && canPushToServer()) {
         onRemoteOperationFailure()
      }
   })
}
```

It may seem like a lot is happening, but it's not. What this does is:

1. Perform local operation.
2. Try to push changes. If successful, the operation is successful.
3. If pushing fails, `onRemoteOperationFailure()` will be triggered if the sync type is set to `Client-to-Server` or
   `Two-Way Sync`.

Now we need to figure out how to save the operations locally when there's a failure on the remote server (mostly because
the server is down), so once the server is up, Linkora App can send those operations.

For that, I have a table called `PendingSyncQueue`:

```kotlin
@Entity("pending_sync_queue")
data class PendingSyncQueue(
   @PrimaryKey(autoGenerate = true) val id: Long = 0,
   val operation: String,
   val payload: String
)
```

Now, the `operation` refers to the endpoint at which the operation needs to be performed, and the `payload` is the body
of the POST request.

A simple example of how this is done:

```kotlin
onRemoteOperationFailure = {
   pendingSyncQueueRepo.addInQueue(
      PendingSyncQueue(
         operation = RemoteRoute.Link.ARCHIVE_LINK.name,
         payload = Json.encodeToString(
            IDBasedDTO(
               linkId, eventTimestamp
            )
         )
      )
   )
}
```

Where *every* DTO contains `correlation`. Here, the `IDBasedDTO` looks like:

```kotlin
@Serializable
data class IDBasedDTO(
   val id: Long,
   val eventTimestamp: Long,
   val correlation: Correlation = AppPreferences.getCorrelation(),
)

@Serializable
data class Correlation(
   val id: String, val clientName: String
)
```

`Correlation` helps in identifying the client which performs the operation, because we don't want to perform locally
after reading remote updates if that update was performed by us. If done by a different client, it won't match our
`Correlation`, so we can perform that knowing we're not the source.

Now, once the server and app are both online, we can send queued data from `PendingSyncQueue`. For the same example
considered earlier, here's how it will be sent:

```kotlin
when (queue.operation) {
   ARCHIVE_LINK.name -> {
      val idBasedDTO = Json.decodeFromString<IDBasedDTO>(queueItem.payload)
      val remoteLinkId = localLinksRepo.getRemoteLinkId(idBasedDTO.id)
      remoteLinksRepo.archiveALink(idBasedDTO.copy(id = remoteLinkId))
         .removeQueueItemAndSyncTimestamp(queueItem.id)
   }
}

private suspend inline fun Flow<Result<TimeStampBasedResponse>>.removeQueueItemAndSyncTimestamp(
   queueId: Long
) {
   this.collectLatest {
      it.onSuccess {
         pendingSyncQueueRepo.removeFromQueue(queueId)
         preferencesRepository.updateLastSyncedWithServerTimeStamp(it.data.eventTimestamp)
      }
   }
}

@Serializable
data class TimeStampBasedResponse(
   val eventTimestamp: Long,
   val message: String
)
```

This way, we can confirm the client will definitely send the data to the server (if it gets uninstalled, we can't do
anything about it).

In conclusion, the following image should give you a clear idea of how all these components work together to ensure
`Client-to-Server` sync works as expected:
![client to server in linkora](https://i.ibb.co/ycHSTyxK/client-to-server.png)

Now on the server-side, LWW (Last Write Wins) is implemented for some routes where updating is required. This makes sure
the server only updates newer values in case all clients and the server aren't up at the same time:

```kotlin
// on server-side
private fun checkForLWWConflictAndThrow(id: Long, timeStamp: Long) {
   transaction {
      FoldersTable.select(FoldersTable.lastModified).where {
         FoldersTable.id.eq(id)
      }.let {
         if (it.single()[FoldersTable.lastModified] > timeStamp) {
            throw LWWConflictException()
         }
      }
   }
}
---
override suspend fun markAsArchive(idBasedDTO: IDBasedDTO): Result<TimeStampBasedResponse> {
   return try {
      checkForLWWConflictAndThrow(id = idBasedDTO.id, timeStamp = idBasedDTO.eventTimestamp)
      // further impl
   } catch (e: Exception) {
      Result.Failure(e)
   }
}
```

To support this, every table contains a column called `lastModified`, which will also be sent in the POST request body
and is needed for the `sync-server`:

```kotlin
@Entity(tableName = "folders")
@Serializable
data class Folder(
   val name: String,
   val note: String,
   val parentFolderId: Long?,
   @PrimaryKey(autoGenerate = true)
   val localId: Long = 0,
   val remoteId: Long? = null,
   val isArchived: Boolean = false,
   val lastModified: Long
)
```

### 2. Server-to-Client

`Client-to-Server` focuses on pushing changes, while `Server-to-Client`
focuses on reading changes that occurred on the remote database through the server.

The app saves a `TIME_STAMP` in its preferences, updated at every successful remote request. The `TIME_STAMP` value is
sent from the server (since server operations happen there).

Changes can be read in two ways:

1. *Using sockets* if both app and server are online.
2. *Custom implementations* if the client is offline or disconnected from the server.

#### 1. Using sockets if both app and server are online

When both app and server are online, it’s simple: use sockets and update as required. Linkora App handles this as
follows:

```kotlin
private suspend fun updateLocalDBAccordingToEvent(
   deserializedWebSocketEvent: WebSocketEvent
) {
   when (deserializedWebSocketEvent.operation) {
      MARK_FOLDER_AS_ARCHIVE.name -> {
         val idBasedDTO = json.decodeFromJsonElement<IDBasedDTO>(
            deserializedWebSocketEvent.payload
         )
         if (idBasedDTO.correlation.isSameAsCurrentClient()) {
            preferencesRepository.updateLastSyncedWithServerTimeStamp(idBasedDTO.eventTimestamp)
            return
         }

         val folderId = localFoldersRepo.getLocalIdOfAFolder(idBasedDTO.id)
         if (folderId != null) {
            localFoldersRepo.markFolderAsArchive(
               folderId, viaSocket = true
            ).collectAndUpdateTimestamp(idBasedDTO.eventTimestamp)
         }
      }
   }
}
```

Similarly handle for every possible operation.

#### 2. Custom implementations if the client is offline or disconnected from the server

We need to handle two scenarios if the client is offline or disconnected from the server:

1. Handling deletions.
2. Updating data after the last known `TIME_STAMP`.

##### 2.1 Handling deletions when offline

We track deleted items using a server-side `Tombstone` table structured as:

```kotlin
object TombstoneTable : LongIdTable("tombstone") {
   val deletedAt = long("deleted_at")
   val operation = text("operation")
   val payload = text("payload")
}
```

The following example should give a brief idea about how this table is used:

```kotlin
transaction {
   TombStoneHelper.insert(
      payload = Json.encodeToString(idBasedDTO),
      operation = LinkRoute.DELETE_A_LINK.name,
      deletedAt = eventTimestamp
   )
   LinksTable.deleteWhere {
      id.eq(idBasedDTO.id)
   }
}
```

And now on the client side, when both the app and server are online, we pull these tombstone records and delete the
corresponding items locally.

##### 2.2 Updating data after the last known `TIME_STAMP`

As mentioned earlier, the local database in the app contains a column called `lastModified`. Similarly, tables in the
remote database also include this column. The app sends its last known `TIME_STAMP` to the server, which returns all
changes made after that timestamp:

```kotlin
LinksTable.selectAll().where {
   LinksTable.lastModified.greater(TIME_STAMP)
}.toList().forEach {
   updatedLinks.add(
      Link(
         id = it[LinksTable.id].value,
         linkType = LinkType.valueOf(it[LinksTable.linkType]),
         title = it[LinksTable.linkTitle],
         url = it[LinksTable.url],
         baseURL = it[LinksTable.baseURL],
         imgURL = it[LinksTable.imgURL],
         note = it[LinksTable.note],
         idOfLinkedFolder = it[LinksTable.idOfLinkedFolder],
         userAgent = it[LinksTable.userAgent],
         markedAsImportant = it[LinksTable.markedAsImportant],
         mediaType = MediaType.valueOf(it[LinksTable.mediaType]),
         eventTimestamp = it[LinksTable.lastModified]
      )
   )
}
```

Now the collected updates will be sent back to client, which it will update accordingly.

In conclusion, the following images should give you a clear idea of how all these components work together to make sure
`Server-to-Client` sync operates as expected:

1. If both app and server are online.
   ![](https://i.ibb.co/21j6MPtG/server-to-client-with-socket.png)
2. If the client is offline or disconnected from the server.
   ![](https://i.ibb.co/N2KfKPcw/server-to-client-with-manual.png)
---
Overall, this is how synchronization works in Linkora. These operations are also used when performing manual syncing or importing data from external files, but that is outside the context of this topic, hence I didn't include it.