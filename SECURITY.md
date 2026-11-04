# Security and privacy in Taas Adda

Taas Adda has no servers, no accounts, no ads and no analytics. Nothing about players is sent to the developer. This page lists how the app protects players from each other and from other apps, and what it can't protect against.

## Joining a nearby table

- **The host lets each player in.** Anyone nearby can see an open table, but nobody can join, chat or receive game data until the host taps **Let in**.
- **Matching code.** Nearby Connections shows the same 4-digit code on both phones. The host and the joining friend check that the codes match before letting them in. This stops a stranger's phone from pretending to be the table, or slipping in between two friends.
- **Encrypted link.** Google Play services encrypts the Bluetooth / Wi-Fi connection.
- **Table limit.** At most three guests; requests beyond that are turned away.

## Messages from other phones are treated as untrusted

Every message from another phone is checked before it is used:

- Names, chat, scores and SOS text are shown as plain text, never as page code, so a dishonest player can't inject anything that runs on your phone.
- Card ids, seat numbers, scores and counts must be valid values; anything else is dropped.
- Emoji must come from the app's own list.
- SOS links are accepted only in the exact form `https://maps.google.com/?q=<latitude>,<longitude>`.
- Chat (0.7 s), emoji (0.6 s) and SOS (20 s) are rate-limited per player to stop spam.
- Messages larger than 32 KB are ignored.

These protections were tested by simulating a stranger who tries to talk before being admitted, and a malicious host who sends script injections, fake SOS links and malformed game data. All were blocked.

## The app itself

- **Content Security Policy:** the page may run only the game's own script, identified by its SHA-256 hash, and may not load content or connect anywhere else (except optional display fonts). Even if text slipped through, injected code could not run.
- **Limited bridge to Android:** the page can only ask Android to dial digits-only phone numbers, open the messaging app for numbers you entered, open `https` links, read location when you press SOS, and talk to nearby phones. Any other link type (`javascript:`, `intent:`, `file:`…) is refused.
- **No silent actions:** the app never sends SMS or makes calls by itself. It opens your messaging or phone app with everything filled in, and you press send or call.
- **Location is read only when you press SOS**, with *Include my current location* ticked. The web page has no direct access to location.
- **No backups:** app data (name, settings, emergency contacts) stays on the device and is excluded from cloud backups.
- **No unencrypted traffic, no web debugging, no file access** from the page.
- **Release builds are signed** with a private upload key that is never stored in this repository.

## What it can't prevent

- **The host's phone runs the game**, so it knows everyone's cards. A host using a modified app could cheat. Play nearby games with people you trust. This is normal for host-based local games.
- **Your messages are seen by everyone at your table.** Don't share private information in chat.
- **SOS depends on your phone:** it needs battery, location turned on, and for table alerts, friends nearby with the app open. It does not contact emergency services. In danger, call them directly.

## Reporting a problem

Open an issue at https://github.com/Sharma2040/taas-adda/issues. Please don't post exploit details publicly; describe the area and we will follow up.
