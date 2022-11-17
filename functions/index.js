// [START indexCovoiturageToElastic]
const functions = require('firebase-functions');
const request = require('request-promise')
var BadWords = require('bad-words');
const frenchBadwords = require('french-badwords-list');

exports.TextCensure = functions.database.ref("/Annonces/{annonce_id}")
	.onCreate((snapshot,context)=> {
		const annonce_id = context.params.annonce_id;

		var badwords = new BadWords();
		badwords.addWords(...frenchBadwords.array);

		const annonce_data = snapshot.val();
		const publisher = annonce_data.publisher;
		const text = annonce_data.contenu;

		if(badwords.isProfane(text)){
			var db = admin.database()
			db.ref("Annonces").child(annonce_id).remove();
			console.log("Annonce "+annonce_id+ " de "+ publisher + " supprimé");	
		}
		
	})


exports.getLike = functions.database.ref("Likes/{postid}/{like}")
	.onCreate((snapshot,context)=>{
		let Postid = context.params.postid;
		var nbr = 0;
		var db = admin.database();
		db.ref("Likes").child(Postid).on("value",(snapshot) =>{
			nbr = snapshot.numChildren();
			var dl = admin.database();
			dl.ref("Posts").child(Postid).child("likes").set(nbr);
		})
	})

exports.CovoiturageClean = functions.pubsub.schedule('every 24 hours')
	.onRun((context) => {
		var db = admin.database();
		var dl = admin.database();
		var nbr = 0;
		db.ref('Covoiturage').on('value',(snapshot) =>{
			snapshot.forEach(function(childSnapshot){
				if(isPast(childSnapshot.child("date").val())){
					dl.ref('Covoiturage').child(childSnapshot.child("postid").val()).remove();

					nbr = nbr + 1;

				}
			});
		});
		console.log("CLEANER:"+ nbr +" annonces supprimé(s)");
		return null;
	});

	function isPast(date){
		var crt_date =  new Date(Date.now());
		var crt_month = crt_date.getMonth() + 1;
		var crt_day = crt_date.getDate();
		var month = parseInt(date.split("/")[1]);
		var day = parseInt(date.split("/")[0]);
	
		if(crt_month > month){
			return true
		}else if(crt_month == month){
			if(crt_day > day){
				return true
			}else{
				return false
			}
		}else{
			return false
		}
	}


exports.indexPostsToElastic = functions.database.ref('/Covoiturage/{covoiturage_id}')
	.onWrite((change,context) => {
		let covoiturageData = change.after.val();
		let covoiturage_id = context.params.covoiturage_id;
		
		console.log('Indexing annonce:', covoiturageData);
		
		let elasticSearchConfig = functions.config().elasticsearch;
		let elasticSearchUrl = elasticSearchConfig.url + 'covoiturages/covoiturage/'+ covoiturage_id;
		console.log('url:', elasticSearchUrl);
		let elasticSearchMethod = covoiturageData ? 'POST' : 'DELETE';
		
		let elasticSearchRequest = {
			method: elasticSearchMethod,
			url: elasticSearchUrl,
			auth:{
				username: elasticSearchConfig.username,
				password: elasticSearchConfig.password,
			},
			body: covoiturageData,
			json: true
		};
		
		 return request(elasticSearchRequest).then(response => {
             console.log("ElasticSearch response", response);
             return null;
          });

	});
// [END indexCovoiturageToElastic]

exports.indexAnnonceToElastic = functions.database.ref('/Annonces/{annonce_id}')
	.onWrite((change,context) => {
		let annonceData = change.after.val();
		let annonce_id = context.params.annonce_id;
		
		console.log('Indexing annonce:', annonceData);
		
		let elasticSearchConfig = functions.config().elasticsearch;
		let elasticSearchUrl = elasticSearchConfig.url + 'annonces/annonce/'+ annonce_id;
		console.log('url:', elasticSearchUrl);
		let elasticSearchMethod = annonceData ? 'POST' : 'DELETE';
		
		let elasticSearchRequest = {
			method: elasticSearchMethod,
			url: elasticSearchUrl,
			auth:{
				username: elasticSearchConfig.username,
				password: elasticSearchConfig.password,
			},
			body: annonceData,
			json: true
		};
		
		 return request(elasticSearchRequest).then(response => {
             console.log("ElasticSearch response", response);
             return null;
          });

	});


// [START SendReport]


	const admin = require('firebase-admin');
	const nodemailer = require('nodemailer');
	const cors = require('cors')({origin: true});
	admin.initializeApp();
	
	
	/**
	* Here we're using Gmail to send 
	*/
	let transporter = nodemailer.createTransport({
		host: "ssl0.ovh.net",
  		port: 587,
		auth: {
			user: 'contact@hellohelp.fr',
			pass: 'H@wh3Jh2z9mn9E5'
		}
	});

	exports.deleteUser = functions.https.onCall((data) => { 

		const id = data.user

		var db = admin.database()
		db.ref("Covoiturage").orderByChild("publisher").equalTo(id).on("child_added", function(snapshot) {
		  console.log("Deleted covoiturage id : "+snapshot.key);
		  db.ref("Covoiturage").child(snapshot.key).remove();
		
		})

		var db = admin.database()
		db.ref("Annonces").orderByChild("publisher").equalTo(id).on("child_added", function(snapshot) {
		  console.log("Deleted annonce id : " + snapshot.key);
		  db.ref("Annonces").child(snapshot.key).remove();
		
		})

		var db = admin.database()
		db.ref("Chats").orderByChild("sender").equalTo(id).on("child_added", function(snapshot) {
		  console.log("Deleted chat id : "+ snapshot.key);
		  db.ref("Chats").child(snapshot.key).remove();
		
		})

		var db = admin.database()
		db.ref("Chats").orderByChild("receiver").equalTo(id).on("child_added", function(snapshot) {
		  console.log("Deleted chat id : "+snapshot.key);
		  db.ref("Chats").child(snapshot.key).remove();
		
		})
		
	});

	exports.contact = functions.https.onCall((data) => {
		
		  
		// getting dest email by query string
		const nom = data.nom;
		const email = data.email;
		const sujet = data.sujet;
		const msg = data.msg;
	
		const mailOptions = {
			from: 'Formulaire contact <'+email+'>', // Something like: Jane Doe <janedoe@gmail.com>
			to: 'contact@hellohelp.fr',
			subject: nom + ' pour ' + sujet, // email subject
			text: msg 
		};
	  
		// returning result
		return transporter.sendMail(mailOptions).then(response => {
			console.log("ContactMail response", response);
			return null;
		});
	  
	});
		// [End SendReport]
	
	exports.sendReport = functions.https.onCall((data) => {
		
		  
		// getting dest email by query string
		const user = data.user;
		const motif = data.motif;
		const sender = data.sender;
	
		const mailOptions = {
			from: 'Signalement Utilisateur <contact@hellohelp.fr>', // Something like: Jane Doe <janedoe@gmail.com>
			to: 'signalement.hellohelp@gmail.com',
			subject: sender + ' a signalé ' + user, // email subject
			html: `<html xmlns="http://www.w3.org/1999/xhtml" xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:v="urn:schemas-microsoft-com:vml">
					<head>
					<meta content="text/html; charset=utf-8" http-equiv="Content-Type"/>
					<meta content="width=device-width" name="viewport"/>
					<meta content="IE=edge" http-equiv="X-UA-Compatible"/>
					<title></title>
					<style type="text/css">
							body {
								margin: 0;
								padding: 0;
							}
					
							table,
							td,
							tr {
								vertical-align: top;
								border-collapse: collapse;
							}
					
							* {
								line-height: inherit;
							}
					
							a[x-apple-data-detectors=true] {
								color: inherit !important;
								text-decoration: none !important;
							}
						</style>
					<style id="media-query" type="text/css">
							@media (max-width: 620px) {
					
								.block-grid,
								.col {
									min-width: 320px !important;
									max-width: 100% !important;
									display: block !important;
								}
					
								.block-grid {
									width: 100% !important;
								}
					
								.col {
									width: 100% !important;
								}
					
								.col>div {
									margin: 0 auto;
								}
					
								img.fullwidth,
								img.fullwidthOnMobile {
									max-width: 100% !important;
								}
					
								.no-stack .col {
									min-width: 0 !important;
									display: table-cell !important;
								}
					
								.no-stack.two-up .col {
									width: 50% !important;
								}
					
								.no-stack .col.num4 {
									width: 33% !important;
								}
					
								.no-stack .col.num8 {
									width: 66% !important;
								}
					
								.no-stack .col.num4 {
									width: 33% !important;
								}
					
								.no-stack .col.num3 {
									width: 25% !important;
								}
					
								.no-stack .col.num6 {
									width: 50% !important;
								}
					
								.no-stack .col.num9 {
									width: 75% !important;
								}
					
								.video-block {
									max-width: none !important;
								}
					
								.mobile_hide {
									min-height: 0px;
									max-height: 0px;
									max-width: 0px;
									display: none;
									overflow: hidden;
									font-size: 0px;
								}
					
								.desktop_hide {
									display: block !important;
									max-height: none !important;
								}
							}
						</style>
					</head>
					<body class="clean-body" style="margin: 0; padding: 0; -webkit-text-size-adjust: 100%; background-color: #FFFFFF;">
					<table bgcolor="#11455b" cellpadding="0" cellspacing="0" class="nl-container" role="presentation" style="table-layout: fixed; vertical-align: top; min-width: 320px; Margin: 0 auto; border-spacing: 0; border-collapse: collapse; mso-table-lspace: 0pt; mso-table-rspace: 0pt; background-color: #11455b; width: 100%;" valign="top" width="100%">
					<tbody>
					<tr style="vertical-align: top;" valign="top">
					<td style="word-break: break-word; vertical-align: top;" valign="top">
					<div style="background-color:#F7F7F7;">
					<div class="block-grid" style="Margin: 0 auto; min-width: 320px; max-width: 1200px; overflow-wrap: break-word; word-wrap: break-word; word-break: break-word; background-color: #11455b;">
					<div style="border-collapse: collapse;display: table;width: 100%;background-color:#11455b;">
					<div class="col num12" style="min-width: 320px; max-width: 1200px; display: table-cell; vertical-align: top; width: 1200px;">
					<div style="width:100% !important;">
					<div style="border-top:0px solid transparent; border-left:0px solid transparent; border-bottom:0px solid transparent; border-right:0px solid transparent; padding-top:0px; padding-bottom:0px; padding-right: 0px; padding-left: 0px;">
					<div align="center" class="img-container center autowidth" style="padding-right: 25px;padding-left: 25px;">
					<div style="font-size:1px;line-height:25px"> </div><img align="center" alt="Image" border="0" class="center autowidth" src="https://i.imgur.com/vnHYt5I.png" style="text-decoration: none; -ms-interpolation-mode: bicubic; border: 0; height: auto; width: 100%; max-width: 400px; display: block;" title="Image" width="400"/>
					<div style="font-size:1px;line-height:25px"> </div>
					</div>
					</div>
					</div>
					</div>
					</div>
					</div>
					</div>
					<div style="background-color:#FFFFFF;">
					<div class="block-grid" style="Margin: 0 auto; min-width: 320px; max-width: 1200px; overflow-wrap: break-word; word-wrap: break-word; word-break: break-word; background-color: #1d6b8c;">
					<div style="border-collapse: collapse;display: table;width: 100%;background-color:#DC1D1D;">
					<div class="col num12" style="min-width: 320px; max-width: 1200px; display: table-cell; vertical-align: top; width: 1200px;">
					<div style="width:100% !important;">
					<div style="border-top:0px solid transparent; border-left:0px solid transparent; border-bottom:0px solid transparent; border-right:0px solid transparent; padding-top:0px; padding-bottom:0px; padding-right: 0px; padding-left: 0px;">
					<div style="color:#FFFFFF;font-family:Helvetica, sans-serif;line-height:1.2;padding-top:30px;padding-right:20px;padding-bottom:20px;padding-left:20px;">
					<div style="line-height: 1.2; font-size: 12px; font-family: Helvetica, sans-serif; color: #FFFFFF; mso-line-height-alt: 14px;">
					<p style="font-size: 20px; line-height: 1.2; text-align: center; word-break: break-word; font-family: Helvetica, sans-serif; mso-line-height-alt: 22px; margin: 0;">⚠ <strong>[Signalement]</strong> ⚠</p>
					</div>
					</div>
					</div>
					</div>
					</div>
					</div>
					</div>
					</div>
					<div style="background-color:#FFFFFF;">
					<div class="block-grid" style="Margin: 0 auto; min-width: 320px; max-width: 1200px; overflow-wrap: break-word; word-wrap: break-word; word-break: break-word; background-color: #FFFFFF;">
					<div style="border-collapse: collapse;display: table;width: 100%;background-color:#FFFFFF;">
					<div class="col num12" style="min-width: 320px; max-width: 1200px; display: table-cell; vertical-align: top; width: 1200px;">
					<div style="width:100% !important;">
					<div style="border-top:0px solid transparent; border-left:0px solid transparent; border-bottom:0px solid transparent; border-right:0px solid transparent; padding-top:0px; padding-bottom:10px; padding-right: 0px; padding-left: 0px;">
					<div style="color:#283C4B;font-family:Helvetica, sans-serif;line-height:1.5;padding-top:10px;padding-right:30px;padding-bottom:10px;padding-left:30px;">
					<div style="line-height: 1.5; font-size: 12px; font-family: Helvetica, sans-serif; color: #444444; mso-line-height-alt: 18px;">
					<p style="font-size: 18px; line-height: 1.5; word-break: break-word; text-align: center; font-family: Helvetica, sans-serif; mso-line-height-alt: 24px; margin: 0;"><span style="background-color: transparent; font-size: 18px;"><span style=""> <strong>${sender} </strong> a été signalé par <strong>${user}</strong></span></span></p>
					<p style="font-size: 16px; line-height: 1.5; word-break: break-word; text-align: left; font-family: Helvetica, sans-serif; mso-line-height-alt: 24px; margin: 0;"><span style="background-color: transparent; font-size: 16px;"><span style=""><strong>Pour le motif:</strong></span></span></p>
					</div>
					</div>
					<div style="color:#283C4B;font-family:Helvetica, sans-serif;line-height:1.5;padding-right:30px;padding-bottom:0px;padding-left:30px;">
					<div style="line-height: 1.5; font-size: 12px; font-family: Helvetica, sans-serif; color: #4f4f4f; mso-line-height-alt: 18px;"><span style="font-size: 14px;">${motif}</span></div>
					</div>
					</div>
					</div>
					</div>
					</div>
					</div>
					</div>
					<div style="background-color:#F7F7F7;">
					<div class="block-grid" style="Margin: 0 auto; min-width: 320px; max-width: 1200px; overflow-wrap: break-word; word-wrap: break-word; word-break: break-word; background-color: transparent;">
					<div style="border-collapse: collapse;display: table;width: 100%;background-color:#11455b;">
					<div class="col num12" style="min-width: 320px; max-width: 1200px; display: table-cell; vertical-align: top; width: 1200px;">
					<div style="width:100% !important;">
					<div style="border-top:0px solid transparent; border-left:0px solid transparent; border-bottom:0px solid transparent; border-right:0px solid transparent; padding-bottom:10px; padding-right: 0px; padding-left: 0px;">
					<table cellpadding="0" cellspacing="0" class="social_icons" role="presentation" style="table-layout: fixed; vertical-align: top; border-spacing: 0; border-collapse: collapse; mso-table-lspace: 0pt; mso-table-rspace: 0pt;" valign="top" width="100%">
					<tbody>
					<tr style="vertical-align: top;" valign="top">
					<td style="word-break: break-word; vertical-align: top; padding-right: 10px; padding-bottom: 10px; padding-left: 10px;" valign="top"> 
					<!--[if mso]><table width="100%" cellpadding="0" cellspacing="0" border="0"><tr><td style="padding-right: 30px; padding-left: 30px; padding-top: 10px; padding-bottom: 0px; font-family: Arial, sans-serif"><![endif]-->
					<div style="color:#ffffff;font-family:Helvetica, sans-serif;line-height:1.5;padding-top:25px;padding-right:30px;padding-bottom:0px;padding-left:30px;">
					<div style="line-height: 1.5; font-size: 12px; font-family: Helvetica, sans-serif; text-align: center; color: #ffffff; mso-line-height-alt: 18px;"><span style="font-size: 10px;">© 2020 - Hello Help - All rights reserved</span></div>
					</div>
					</div>
					</div>
					</div>
					</div>
					</div>
					</td>
					</tr>
					</tbody>
					</table>
					</body>
					</html>
				  ` // email content in HTML
			};
	  
			// returning result
			return transporter.sendMail(mailOptions).then(response => {
				console.log("sendMail response", response);
				return null;
			});
		  
	});
