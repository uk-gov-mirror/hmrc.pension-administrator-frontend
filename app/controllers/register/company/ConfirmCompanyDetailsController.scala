/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.register.company

import config.FrontendAppConfig
import connectors.{RegistrationConnector, UserAnswersCacheConnector}
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import forms.register.company.CompanyAddressFormProvider
import identifiers.TypedIdentifier
import identifiers.register.company.ConfirmCompanyAddressId
import identifiers.register.{BusinessNameId, BusinessTypeId, BusinessUTRId, RegistrationInfoId}
import javax.inject.Inject
import models._
import models.requests.DataRequest
import play.api.Logger
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsResultException, Writes}
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.RegisterCompany
import utils.countryOptions.CountryOptions
import utils.{Navigator, UserAnswers}
import views.html.register.company.confirmCompanyDetails

import scala.concurrent.{ExecutionContext, Future}

class ConfirmCompanyDetailsController @Inject()(appConfig: FrontendAppConfig,
                                                override val messagesApi: MessagesApi,
                                                dataCacheConnector: UserAnswersCacheConnector,
                                                @RegisterCompany navigator: Navigator,
                                                authenticate: AuthAction,
                                                allowAccess: AllowAccessActionProvider,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                registrationConnector: RegistrationConnector,
                                                formProvider: CompanyAddressFormProvider,
                                                countryOptions: CountryOptions
                                               )(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport with Retrievals {

  private val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
    implicit request =>
      getCompanyDetails(mode) { registration =>
        upsert(request.userAnswers, ConfirmCompanyAddressId)(registration.response.address) { userAnswers =>
          upsert(userAnswers, BusinessNameId)(registration.response.organisation.organisationName) { userAnswers =>
            upsert(userAnswers, RegistrationInfoId)(registration.info) { userAnswers =>
              dataCacheConnector.upsert(request.externalId, userAnswers.json).flatMap { _ =>

                Future.successful(
                  Ok(
                    confirmCompanyDetails(
                      appConfig,
                      form,
                      registration.response.address,
                      registration.response.organisation.organisationName,
                      countryOptions
                    )
                  )
                )
              }
            }
          }
        }
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>

        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            (BusinessNameId and ConfirmCompanyAddressId).retrieve.right.map {
              case name ~ address =>
                Future.successful(
                  BadRequest(
                    confirmCompanyDetails(
                      appConfig,
                      formWithErrors,
                      address,
                      name,
                      countryOptions
                    )
                  )
                )
            },
          {
            case true =>
              Future.successful(Redirect(navigator.nextPage(ConfirmCompanyAddressId, mode, request.userAnswers)))
            case false =>


              val updatedAnswers = request.userAnswers.removeAllOf(List(ConfirmCompanyAddressId, RegistrationInfoId)).asOpt.getOrElse(request.userAnswers)
              dataCacheConnector.upsert(request.externalId, updatedAnswers.json).flatMap { _ =>
                Future.successful(Redirect(routes.CompanyUpdateDetailsController.onPageLoad()))
              }
          }
        )
  }

  private def getCompanyDetails(mode: Mode)
                               (fn: OrganizationRegistration => Future[Result])
                               (implicit request: DataRequest[AnyContent]) = {
    (BusinessNameId and BusinessUTRId and BusinessTypeId).retrieve.right.map {
      case businessName ~ utr ~ businessType =>
        val organisation = Organisation(businessName.replaceAll("""[^a-zA-Z0-9 '&\/]+""", ""), businessType)
        val legalStatus = RegistrationLegalStatus.LimitedCompany
        registrationConnector.registerWithIdOrganisation(utr, organisation, legalStatus).flatMap {
          registration =>
            fn(registration)
        } recoverWith {
          case _: NotFoundException =>
            Future.successful(Redirect(routes.CompanyNotFoundController.onPageLoad()))
        }
    }
  }

  private def upsert[I <: TypedIdentifier.PathDependent](userAnswers: UserAnswers, id: I)(value: id.Data)
                                                        (fn: UserAnswers => Future[Result])
                                                        (implicit writes: Writes[id.Data]) = {

    userAnswers
      .set(id)(value)
      .fold(
        errors => {
          Logger.error("Unable to set user answer", JsResultException(errors))
          Future.successful(InternalServerError)
        },
        userAnswers => fn(userAnswers)
      )
  }

}
