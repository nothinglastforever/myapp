import app.DatabaseUsernamePasswordAuthenticator

import org.pac4j.http.client.indirect.FormClient

import org.pac4j.http.profile.creator.AuthenticatorProfileCreator


import static ratpack.groovy.Groovy.ratpack
import static ratpack.groovy.Groovy.groovyTemplate
import static java.util.Collections.singletonMap

import ratpack.session.SessionModule
import ratpack.pac4j.RatpackPac4j
import ratpack.groovy.template.TextTemplateModule

import groovy.sql.Sql
import ratpack.groovy.sql.SqlModule
import ratpack.hikari.HikariModule

import ratpack.service.Service
import ratpack.service.StartEvent

ratpack {
  bindings {
    module SessionModule
    module TextTemplateModule // <1>
	module SqlModule
	module(HikariModule) { c ->
	  c.dataSourceClassName = 'com.mysql.jdbc.jdbc2.optional.MysqlDataSource'
	  c.addDataSourceProperty 'URL', 'jdbc:mysql://localhost:3306/examdb'
	  c.username = 'examadmin'
	  c.password = 'examadmin'
	}
		
	bind(DatabaseUsernamePasswordAuthenticator)
  }
  handlers {
		  
    def formClient = new FormClient( // <2>
      "auth",
      registry.get(DatabaseUsernamePasswordAuthenticator),
      AuthenticatorProfileCreator.INSTANCE
    )

    all(RatpackPac4j.authenticator("auth", formClient)) // <3>

    get("login") { // <4>
      render(groovyTemplate(
              singletonMap("callbackUrl", formClient.loginUrl),
              "login.html"))    
    }

    get("logout") { // <5>
      RatpackPac4j.logout(context).then {
        redirect '/'
      }
    }

    get { // <6>
      RatpackPac4j.userProfile(context)
        .route { o -> o.present } { o -> 
          render(groovyTemplate([profile: o.get()], "protectedIndex.html"))
        }
        .then {
          render(groovyTemplate([:], "index.html"))
        }
    }
  }
}
