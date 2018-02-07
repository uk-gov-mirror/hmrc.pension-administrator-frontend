#!/bin/bash

echo "Applying migration $className;format="snake"$"

echo "Adding routes to $routeFile$.routes"

echo "" >> ../conf/$routeFile$.routes
echo "GET        /$className;format="decap"$               controllers.$routeFile$.$className$Controller.onPageLoad(mode: Mode = NormalMode)" >> ../conf/$routeFile$.routes
echo "POST       /$className;format="decap"$               controllers.$routeFile$.$className$Controller.onSubmit(mode: Mode = NormalMode)" >> ../conf/$routeFile$.routes

echo "GET        /change$className$                        controllers.$routeFile$.$className$Controller.onPageLoad(mode: Mode = CheckMode)" >> ../conf/$routeFile$.routes
echo "POST       /change$className$                        controllers.$routeFile$.$className$Controller.onSubmit(mode: Mode = CheckMode)" >> ../conf/$routeFile$.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "$className;format="decap"$.title = $className;format="decap"$" >> ../conf/messages.en
echo "$className;format="decap"$.heading = $className;format="decap"$" >> ../conf/messages.en
echo "$className;format="decap"$.checkYourAnswersLabel = $className;format="decap"$" >> ../conf/messages.en
echo "$className;format="decap"$.error__required = Please give an answer for $className;format="decap"$" >> ../conf/messages.en

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def $className;format="decap"$: Option[AnswerRow] = userAnswers.get(identifiers.$routeFile$.$className$Id) map {";\
     print "    x => AnswerRow(\"$className;format="decap"$.checkYourAnswersLabel\", s\"\$x\", false, controllers.$routeFile$.routes.$className$Controller.onPageLoad(CheckMode).url)";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration $className;format="snake"$ completed"
