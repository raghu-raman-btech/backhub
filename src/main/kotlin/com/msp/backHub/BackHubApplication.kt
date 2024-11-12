package com.msp.backHub

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@SpringBootApplication
@ComponentScan
@Configuration
class BackHubApplication : SpringBootServletInitializer()

fun main(args: Array<String>) {
	runApplication<BackHubApplication>(*args)
}






















// HTTP :

//inputJson now:
//{
//	"version": "1.0",
//	"resource": "/MyOwn",
//	"path": "/default/MyOwn",
//	"httpMethod": "POST",
//	"headers": {
//	"Content-Length": "38",
//	"Content-Type": "application/json",
//	"Host": "apkuzwo7xe.execute-api.ap-southeast-2.amazonaws.com",
//	"Postman-Token": "09c69ba7-2098-412e-86fa-b95617f6571b",
//	"User-Agent": "PostmanRuntime/7.41.2",
//	"X-Amzn-Trace-Id": "Root=1-66d1bc87-4b7320fe1d7a94064a0c5530",
//	"X-Forwarded-For": "60.243.42.3",
//	"X-Forwarded-Port": "443",
//	"X-Forwarded-Proto": "https",
//	"accept": "*/*",
//	"accept-encoding": "gzip, deflate, br"
//},
//	"multiValueHeaders": {
//	"Content-Length": [
//	"38"
//	],
//	"Content-Type": [
//	"application/json"
//	],
//	"Host": [
//	"apkuzwo7xe.execute-api.ap-southeast-2.amazonaws.com"
//	],
//	"Postman-Token": [
//	"09c69ba7-2098-412e-86fa-b95617f6571b"
//	],
//	"User-Agent": [
//	"PostmanRuntime/7.41.2"
//	],
//	"X-Amzn-Trace-Id": [
//	"Root=1-66d1bc87-4b7320fe1d7a94064a0c5530"
//	],
//	"X-Forwarded-For": [
//	"60.243.42.3"
//	],
//	"X-Forwarded-Port": [
//	"443"
//	],
//	"X-Forwarded-Proto": [
//	"https"
//	],
//	"accept": [
//	"*/*"
//	],
//	"accept-encoding": [
//	"gzip, deflate, br"
//	]
//},
//	"queryStringParameters": null,
//	"multiValueQueryStringParameters": null,
//	"requestContext": {
//	"accountId": "010526266367",
//	"apiId": "apkuzwo7xe",
//	"domainName": "apkuzwo7xe.execute-api.ap-southeast-2.amazonaws.com",
//	"domainPrefix": "apkuzwo7xe",
//	"extendedRequestId": "dUplMgrsywMEJTw=",
//	"httpMethod": "POST",
//	"identity": {
//	"accessKey": null,
//	"accountId": null,
//	"caller": null,
//	"cognitoAmr": null,
//	"cognitoAuthenticationProvider": null,
//	"cognitoAuthenticationType": null,
//	"cognitoIdentityId": null,
//	"cognitoIdentityPoolId": null,
//	"principalOrgId": null,
//	"sourceIp": "60.243.42.3",
//	"user": null,
//	"userAgent": "PostmanRuntime/7.41.2",
//	"userArn": null
//},
//	"path": "/default/MyOwn",
//	"protocol": "HTTP/1.1",
//	"requestId": "dUplMgrsywMEJTw=",
//	"requestTime": "30/Aug/2024:12:35:19 +0000",
//	"requestTimeEpoch": 1725021319344,
//	"resourceId": "ANY /MyOwn",
//	"resourcePath": "/MyOwn",
//	"stage": "default"
//},
//	"pathParameters": null,
//	"stageVariables": null,
//	"body": "{\n    \"body\":{\n    \"someData\":\"abc\"}\n}",
//	"isBase64Encoded": false
//}


// Connections - routes :

//inputJson now:
//{
//	"headers": {
//	"Host": "54zteho481.execute-api.ap-southeast-2.amazonaws.com",
//	"Sec-WebSocket-Key": "dAqgy61pbMucW08rdMU5yA==",
//	"Sec-WebSocket-Version": "13",
//	"User-Agent": "Go-http-client/1.1",
//	"X-Amzn-Trace-Id": "Root=1-66d1bcce-1d155b9f01efea8d7ab29235",
//	"X-Forwarded-For": "60.243.42.3",
//	"X-Forwarded-Port": "443",
//	"X-Forwarded-Proto": "https"
//},
//	"multiValueHeaders": {
//	"Host": [
//	"54zteho481.execute-api.ap-southeast-2.amazonaws.com"
//	],
//	"Sec-WebSocket-Key": [
//	"dAqgy61pbMucW08rdMU5yA=="
//	],
//	"Sec-WebSocket-Version": [
//	"13"
//	],
//	"User-Agent": [
//	"Go-http-client/1.1"
//	],
//	"X-Amzn-Trace-Id": [
//	"Root=1-66d1bcce-1d155b9f01efea8d7ab29235"
//	],
//	"X-Forwarded-For": [
//	"60.243.42.3"
//	],
//	"X-Forwarded-Port": [
//	"443"
//	],
//	"X-Forwarded-Proto": [
//	"https"
//	]
//},
//	"requestContext": {
//	"routeKey": "$connect",
//	"eventType": "CONNECT",
//	"extendedRequestId": "dUpwUHKUSwMFsXw=",
//	"requestTime": "30/Aug/2024:12:36:30 +0000",
//	"messageDirection": "IN",
//	"stage": "Learning",
//	"connectedAt": 1725021390511,
//	"requestTimeEpoch": 1725021390517,
//	"identity": {
//	"userAgent": "Go-http-client/1.1",
//	"sourceIp": "60.243.42.3"
//},
//	"requestId": "dUpwUHKUSwMFsXw=",
//	"domainName": "54zteho481.execute-api.ap-southeast-2.amazonaws.com",
//	"connectionId": "dUpwUe-tywMCFvw=",
//	"apiId": "54zteho481"
//},
//	"isBase64Encoded": false
//}


// agent pings :
//inputJson now:
//{
//	"requestContext": {
//	"routeKey": "test",
//	"messageId": "dUpwVe-uSwMCFvw=",
//	"eventType": "MESSAGE",
//	"extendedRequestId": "dUpwVGqHSwMFuOg=",
//	"requestTime": "30/Aug/2024:12:36:30 +0000",
//	"messageDirection": "IN",
//	"stage": "Learning",
//	"connectedAt": 1725021390511,
//	"requestTimeEpoch": 1725021390688,
//	"identity": {
//	"userAgent": "Go-http-client/1.1",
//	"sourceIp": "60.243.42.3"
//},
//	"requestId": "dUpwVGqHSwMFuOg=",
//	"domainName": "54zteho481.execute-api.ap-southeast-2.amazonaws.com",
//	"connectionId": "dUpwUe-tywMCFvw=",
//	"apiId": "54zteho481"
//},
//	"body": "{\"27Aug\":\"1\",\"route\":\"test\"}",
//	"isBase64Encoded": false
//}



//
//<script>
//// Function to get a cookie by name
//function getCookie(name) {
//	const value = `; ${document.cookie}`;
//	const parts = value.split(`; ${name}=`);
//	if (parts.length === 2) return parts.pop().split(';').shift();
//}
//
//// Function to log the entire cookie string
//function logCookies() {
//	console.log("All Cookies:", document.cookie);
//}
//
//// Function to fetch policies based on companyId from the cookie
//function fetchPolicies() {
//	// Get companyId from the cookie
//	const companyId = getCookie('companyId');
//
//	if (companyId) {
//		console.log(`Company ID from cookie: ${companyId}`);
//
//		// Log the entire cookie string for debugging
//		logCookies();
//
//		// Make a request to /policies/{companyId}
//		fetch(`/policies/${companyId}`)
//				.then(response => response.json())
//		.then(data => {
//			console.log('Policies:', data);
//			// Handle the data (e.g., display on the page)
//		})
//		.catch(error => {
//			console.error('Error fetching policies:', error);
//		});
//	} else {
//		console.error('Company ID cookie not found');
//	}
//}
//
//// Call the function when the page loads
//window.onload = function() {
//	fetchPolicies();
//};
//</script>
