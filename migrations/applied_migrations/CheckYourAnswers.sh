#!/bin/bash

echo "Applying migration CheckYourAnswers"

echo "Adding routes to register.advisor.routes"
echo "" >> ../conf/register.advisor.routes
echo "GET        /checkYourAnswers                       controllers.register.advisor.CheckYourAnswersController.onPageLoad()" >> ../conf/register.advisor.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "checkYourAnswers.title = checkYourAnswers" >> ../conf/messages.en
echo "checkYourAnswers.heading = checkYourAnswers" >> ../conf/messages.en

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration CheckYourAnswers completed"
