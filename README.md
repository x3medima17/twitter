# Twitter

Twitter-like app based on Hyperledger Iroha.
This is an example how you can use Iroha to publish messages, based on block streaming feature. 


### Prerequisites

You will need:
* Docker
* Intellij IDEA

## Getting Started

Clone this repository and open it as IDEA project. 
While it is fetching all necessary libs open the terminal and run an Iroha instance.
```
cd deploy
docker-compose up
```
Then run `main.kt` file. It will start listening for iroha events (blocks).

Then run `post.kt` file. It will send a transaction that sets an account detail, which will be later 
caught by the listener and printed.
That's it, event-based app on Hyperledger Iroha.