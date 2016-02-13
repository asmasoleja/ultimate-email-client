Many people who use email regularly receive a steady stream of incoming
messages - maybe 100 messages per day. In fact, as email drives much of the
work we do, so we tend to store most emails, either to remind and inform us of
what needs doing, or just for future reference. Well organised email users
store messages in folders of related topics. But it can be hard to be properly
systematic about this, so sometimes message get mis-filed and thus cannot be
found easily again. And as threads of email conversation develop, it can
become clear that a decision about where to save previous emails was wrong,
tempting the user to move those emails to a new or different folder. And then
some emails are about two topics -- should those two folders be joined?

The philosophy behind this project is centred on the belief that the email
folder is a dead idea. Instead we want our messages to be stored in a database
with easy retrieval. Email users should not have to worry about where emails
are stored, or whether and when they should be deleted. But they should be
able to attach meaningful semantics to messages, in a convenient and
semi-automated way, so that wherever they may be physically stored, they can
be retrieved easily. On the other hand, the email reader does need to have
some way of viewing a sub-set of the emails -- a kind of virtual folder. This
would be the result of a database query, set up and saved by the user.

The project would start by implementing a basic email client in Java,
using available existing libraries. Simultaneously, a study of what features
people want would be undertaken. And then the fun begins...
The ultimate implementation will be able to `suck in' existing email
folders, in various formats, without destroying them, and handle new incoming
messages -- saving them safely in a form so they can, if necessary, be
accessed by traditional clients. Semantic information would be stored, along
with the email index, in a database. And finally, to permit portability of
that information, a sever would be built that can be accessed across the
internet so the client can combine IMAP email access with remote semantic
information access.