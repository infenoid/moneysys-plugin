# MoneySystem - (Zee)

> Introducing our currency “Zee” to your SMP economy.

## 📦 Features
- Player bank accounts  
- Item ↔ Zee exchange rates  
- Leaderboard of richest players  

## 🚀 Installation
1. Download the latest `money-1.0.jar` from the [Releases](https://github.com/infenoid/moneysys-plugin/releases).
2. Drop `moneysys-plugin.jar` in `/plugins`.  
3. Restart your server.

## ⚙️ Commands
/bank pay <player> <amount>

Transfer Zee to another player.

/bank balance

Check your Zee balance.

/bank rich

View the top 5 richest players.

/bank exchange <item> <qty>

Convert items into Zee.


/bank reexchange <item> <qty>

Convert Zee back into items.

/bank value <item>

See current item ↔ Zee rate.

## 🤝 Donate
UPI - satwikg@fam

## 🔒 Permissions
| Node            | Description                     | Default |
| --------------- | ------------------------------- | :-----: |
| `bank.use`      | All `/bank` commands            |   `op`  |
| `bank.pay`      | `/bank pay`                     |   `op`  |
| `bank.exchange` | `/bank exchange`, `/bank value` |   `op`  |
