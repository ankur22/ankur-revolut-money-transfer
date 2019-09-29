# Ankur's Revolut Money Transfer Micro Service

## Building and Testing

Run the following from the projects working directory:

```
mvn clean package
```

## Running the Service

Run the following from the projects working directory:

```
java -jar ./target/moneytransfer-1.0.0.jar
```
 
## API

The API is listed below with the requests and expected respoonses. There is a file called `MondayTransfer.postman_collection.json` which can be imported into postman to try against the running service.

### Create Account

#### Request

End-Point: ```/v1/accounts/```\
Type: ```POST```\
Body:

```
{
	"firstName": "Ankur",
	"otherName": "",
	"surname": "Agarwal"
}
```

#### Responses

 - Status: ```201```\
Reason: Account created\
Body:

```
{
    "reason":"Success",
    "status":201,
    "firstName":"Ankur",
    "otherName":"",
    "surname":"Agarwal",
    "accountNumber":"000000001",
    "amount":0.0
}
```

 - Status: ```500```\
Reason: Parameters failed validation

```
{
	"reason": "Please try again. Unexpected conflict."
}
```


### Get Account Details

#### Request

End-Point: ```/v1/accounts/<accountNumber>```\
Type: ```GET```

#### Responses

 - Status: ```200```\
Reason: Account details found and returned\
Body:

```
{
    "reason":"Success",
    "status":200,
    "firstName":"Ankur",
    "otherName":"",
    "surname":"Agarwal",
    "accountNumber":"000000001",
    "amount":0.0
}
```

 - Status: ```404```\
Reason: No account with specified accountNumber

```
{
	reason: "No account with specified accountNumber"
}
```

### Get Total Amount in Account

#### Request

End-Point: ```/v1/accounts/<accountNumber>/money```\
Type: ```GET```

#### Responses

 - Status: ```200```\
Reason: Account details found and returned\
Body:

```
{
    "reason":"Success",
    "status":200,
	"amount":"0.00"
}
```

 - Status: ```404```\
Reason: No account with specified accountNumber

```
{
	reason: "No account with specified accountNumber"
}
```

### Add Money to Account

#### Request

End-Point: ```/v1/accounts/<accountNumber>/money```\
Type: ```PATCH```\
Body:

```
{
	amountToAdd: "10.00",
	requestId: "589ee00f5418b75a9e7f51e6defd64e6"
}
```

#### Responses

 - Status: ```200```\
Reason: Money added to account\
Body:

```
{
    "reason":"Funds added",
    "status":200,
	"amount":"10.00"
}
```

 - Status: ```400```\
Reason: Float overflow
```
{
	"reason": "Too much money in account",
    "code": 400
}
```

 - Status: ```400```\
Reason: Not enough funds in account

```
{
	"reason": "Not enough funds in account",
    "code": 400
}
```

 - Status: ```409```\
Reason: Request with this requestID has already been processed

```
{
	"reason": "Transaction already complete",
    "code": 409
}
```

 - Status: ```404```\
Reason: No account with specified accountNumber

```
{
	"reason": "No account with specified accountNumber",
    "code": 404
}
```

 - Status: ```408```\
Reason: DB lock could not be acquired so transaction and started

```
{
	"reason": "Timed out, please try again",
    "code": 400
}
```

 - Status: ```500```\
Reason: Unknown response from the DB

```
{
	"reason": "Unknown error when adding funds",
    "code": 500
}
```

### Transfer Money

#### Request

End-Point: ```/v1/accounts/<accountNumber>/money/transfer```\
Type: ```PATCH```\
Body:

```
{
	"amountToTransfer": "9.00",
	"to": "290348745",
	"requestId": "a1380dcf4ddf66aa23994fd1228ddb57"
}
```

#### Responses

 - Status: ```200```\
Reason: Money transferred to specified account\
Body:

```
{
    "reason":"Success",
    "status":200,
    "amount":"10.00"
}
```

 - Status: ```400```\
Reason: Float overflow
```
{
	"reason": "Too much money in account",
    "code": 400
}
```

 - Status: ```400```\
Reason: Not enough funds in account

```
{
	"reason": "Not enough funds in account",
    "code": 400
}
```

 - Status: ```400```\
Reason: Transfer within the same account is not allowed

```
{
	"reason": "Void transaction within same account",
    "code": 400
}
```

 - Status: ```409```\
Reason: Request with this requestID has already been processed

```
{
	"reason": "Transaction already complete",
    "code": 409
}
```

 - Status: ```404```\
Reason: No account with specified accountNumber

```
{
	"reason": "No account with specified accountNumber",
    "code": 404
}
```

 - Status: ```404```\
Reason: No destination account with specified accountNumber

```
{
	"reason": "Destination account not found",
    "code": 404
}
```

 - Status: ```408```\
Reason: DB lock could not be acquired so transaction and started

```
{
	"reason": "Timed out, please try again",
    "code": 400
}
```

 - Status: ```500```\
Reason: Unknown response from the DB

```
{
	"reason": "Unknown error when adding funds",
    "code": 500
}
```