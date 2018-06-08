#!/bin/bash

echo "Applying migration IndividualContactAddress"

echo "Adding routes to register.individual.routes"

echo "" >> ../conf/register.individual.routes
echo "GET        /individualContactAddress               controllers.register.individual.IndividualContactAddressController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/register.individual.routes
echo "POST       /individualContactAddress               controllers.register.individual.IndividualContactAddressController.onSubmit(mode: Mode = NormalMode)" >> ../conf/register.individual.routes

echo "GET        /changeIndividualContactAddress                        controllers.register.individual.IndividualContactAddressController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/register.individual.routes
echo "POST       /changeIndividualContactAddress                        controllers.register.individual.IndividualContactAddressController.onSubmit(mode: Mode = CheckMode)" >> ../conf/register.individual.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "individualContactAddress.title = individualContactAddress" >> ../conf/messages.en
echo "individualContactAddress.heading = individualContactAddress" >> ../conf/messages.en
echo "individualContactAddress.checkYourAnswersLabel = individualContactAddress" >> ../conf/messages.en
echo "individualContactAddress.error.required = Please give an answer for individualContactAddress" >> ../conf/messages.en

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def individualContactAddress: Seq[AnswerRow] = userAnswers.get(identifiers.register.individual.IndividualContactAddressId) match {";\
     print "    case Some(x) => Seq(AnswerRow(\"individualContactAddress.checkYourAnswersLabel\", s\"$x\", false, controllers.register.individual.routes.IndividualContactAddressController.onPageLoad(CheckMode).url))";\
     print "    case _ => Nil";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration IndividualContactAddress completed"
