package com.chrishurd.servicemax

import grails.converters.JSON
import org.apache.commons.logging.LogFactory

class ConfigurationController {

    private static final log = LogFactory.getLog(this)

    def domainService
    def jsonService
    def transactionService
    def organizationInfoService
    def moduleService
    def profileService
    def actionService
    def inventoryService
    def wizardService

    def index() {

        def orgList = organizationInfoService.getUserOrgs(domainService.getUserDomain())

        [orgList : orgList]
    }

    def load() {
        def orgInfo = organizationInfoService.getUserOrg(domainService.getUserDomain(), Long.valueOf(params.fromOrg))
        def orgList = organizationInfoService.getUserOrgs(domainService.getUserDomain())
        def types = params.getList('migrateWhat')
        def migrationObjects

        types.each { type ->
            if ("sfmTransaction".equals(type)) {
                migrationObjects = transactionService.getSFMTransactions(orgInfo)
            }
            else if ("module".equals(type)) {
                migrationObjects =  moduleService.getCustomModules(orgInfo)
            }
            else if ("profile".equals(type)) {
                migrationObjects = profileService.getCustomProfiles(orgInfo)
            }
            else if ("sfAction".equals(type)) {
                migrationObjects = actionService.getCustomActions(orgInfo)
            }
            else if ("inventory".equals(type)) {
                migrationObjects = inventoryService.getCustomProcesses(orgInfo)
            }
            else if ("wizard".equals(type)) {
                migrationObjects = wizardService.getCustomWizards(orgInfo)
            }

        }


        [orgInfo : orgInfo, migrationObjects : migrationObjects, orgList: orgList]
    }

    def save() {
        def id = params.objectId
        def type = params.type
        def fromOrg = organizationInfoService.getUserOrg(domainService.getUserDomain(),Long.valueOf(params.fromOrg))
        def toOrg = organizationInfoService.getUserOrg(domainService.getUserDomain(),Long.valueOf(params.toOrg))


        withFormat {
            json {
                def errors
                try {

                    def results
                    if ("sfmTransaction".equals(type)) {
                        results = transactionService.migrateSFMTransaction(fromOrg, toOrg, id)
                    }
                    else if ("module".equals(type)) {
                        results = moduleService.migrateModule(fromOrg, toOrg, id)
                    }
                    else if ("profile".equals(type)) {
                        results = profileService.migrationProfile(fromOrg, toOrg, id)
                    }
                    else if ("sfAction".equals(type)) {
                        results = actionService.migrateAction(fromOrg, toOrg, id)
                    }
                    else if ("inventory".equals(type)) {
                        results = inventoryService.migrateProcess(fromOrg, toOrg, id)
                    }


                    if (results instanceof Set<Object>) {
                        errors = results
                    }

                }
                catch (Exception ex) {
                    log.error("save", ex)
                    render new JSON(jsonService.prepareErrorPostResponse(ex.toString()))
                    return
                }



                if (! errors || errors.isEmpty()) {
                    render new JSON([success : 'true'])
                }
                else {
                    render new JSON(jsonService.prepareErrorPostResponse(errors))
                }


            }
        }



    }
}
