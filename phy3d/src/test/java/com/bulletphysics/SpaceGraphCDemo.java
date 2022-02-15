package com.bulletphysics;


/** https://github.com/automenta/crittergod1.5/ */
public class SpaceGraphCDemo {
    /*
    GL_DialogWindow*	GL_DialogDynamicsWorld::createDialog(int horPos,int vertPos,int dialogWidth,int dialogHeight, const char* dialogTitle )
{
	btBox2dShape* boxShape = new btBox2dShape(btVector3(dialogWidth/2.,dialogHeight/2.,0.4));
	btScalar mass = 100.f;
	btVector3 localInertia;
	boxShape->calculateLocalInertia(mass,localInertia);
	btRigidBody::btRigidBodyConstructionInfo rbInfo(mass,0,boxShape,localInertia);
	btRigidBody* body = new btRigidBody(rbInfo);
	btTransform trans;
	trans.setIdentity();
	trans.setOrigin(btVector3(horPos-m_screenWidth/2+dialogWidth/2, vertPos+m_screenHeight/2.+dialogHeight/2,0.));



	body->setWorldTransform(trans);
	body->setDamping(0.999,0.99);

	//body->setActivationState(ISLAND_SLEEPING);
	body->setLinearFactor(btVector3(1,1,0));
	//body->setAngularFactor(btVector3(0,0,1));
	body->setAngularFactor(btVector3(0,0,0));

	GL_DialogWindow* dialogWindow = new GL_DialogWindow(horPos,vertPos,dialogWidth,dialogHeight,body,dialogTitle);
	m_dialogs.push_back(dialogWindow);
	m_dynamicsWorld->addRigidBody(body);

	return dialogWindow;

}

GL_SliderControl* GL_DialogDynamicsWorld::createSlider(GL_DialogWindow* dialog, const char* sliderText)
{
	btBox2dShape* boxShape = new btBox2dShape(btVector3(6,6,0.4));
	btScalar mass = .1f;
	btVector3 localInertia;
	boxShape->calculateLocalInertia(mass,localInertia);
	btRigidBody::btRigidBodyConstructionInfo rbInfo(mass,0,boxShape,localInertia);
	btRigidBody* body = new btRigidBody(rbInfo);
	btTransform trans;
	trans.setIdentity();
	trans.setOrigin(btVector3(dialog->getDialogHorPos()-m_screenWidth/2+dialog->getDialogWidth()/2, dialog->getDialogVertPos()+m_screenHeight/2.+dialog->getDialogHeight()/2+dialog->getNumControls()*20,-0.2));

	body->setWorldTransform(trans);
	//body->setDamping(0.999,0.99);

	//body->setActivationState(ISLAND_SLEEPING);
	body->setLinearFactor(btVector3(1,1,0));
	//body->setAngularFactor(btVector3(0,0,1));
	body->setAngularFactor(btVector3(0,0,0));

	m_dynamicsWorld->addRigidBody(body);

	btRigidBody* dialogBody = btRigidBody::upcast(dialog->getCollisionObject());
	btAssert(dialogBody);



	btTransform frameInA;
	frameInA.setIdentity();
	btVector3 offset(btVector3(-dialog->getDialogWidth()/2+16,-dialog->getDialogHeight()/2+dialog->getNumControls()*20+36,0.2));
	frameInA.setOrigin(offset);


	btTransform frameInB;
	frameInB.setIdentity();
	//frameInB.setOrigin(-offset/2);
	bool useFrameA = false;

	btScalar lowerLimit = 80;
	btScalar upperLimit = 170;

#if 0
	btGeneric6DofConstraint* constraint = new btGeneric6DofConstraint(*dialogBody,*body,frameInA,frameInB,useFrameA);
	m_dynamicsWorld->addConstraint(constraint,true);
	constraint->setLimit(0,lowerLimit,upperLimit);
#else
	btSliderConstraint* sliderConstraint = new btSliderConstraint(*dialogBody,*body,frameInA,frameInB,true);//useFrameA);
	sliderConstraint->setLowerLinLimit(lowerLimit);
	sliderConstraint->setUpperLinLimit(upperLimit);
	m_dynamicsWorld->addConstraint(sliderConstraint,true);

#endif

	GL_SliderControl* slider = new GL_SliderControl(sliderText, body,dialog,lowerLimit,upperLimit, sliderConstraint);
	body->setUserPointer(slider);
	dialog->addControl(slider);
	return slider;
}


GL_ToggleControl* GL_DialogDynamicsWorld::createToggle(GL_DialogWindow* dialog, const char* toggleText)
{


	btBox2dShape* boxShape = new btBox2dShape(btVector3(6,6,0.4));
	btScalar mass = 0.1f;
	btVector3 localInertia;
	boxShape->calculateLocalInertia(mass,localInertia);
	btRigidBody::btRigidBodyConstructionInfo rbInfo(mass,0,boxShape,localInertia);
	btRigidBody* body = new btRigidBody(rbInfo);
	btTransform trans;
	trans.setIdentity();
	trans.setOrigin(btVector3(dialog->getDialogHorPos()-m_screenWidth/2+dialog->getDialogWidth()/2, dialog->getDialogVertPos()+m_screenHeight/2.+dialog->getDialogHeight()/2+dialog->getNumControls()*20,-0.2));

	body->setWorldTransform(trans);
	body->setDamping(0.999,0.99);

	//body->setActivationState(ISLAND_SLEEPING);
	body->setLinearFactor(btVector3(1,1,0));
	//body->setAngularFactor(btVector3(0,0,1));
	body->setAngularFactor(btVector3(0,0,0));

	m_dynamicsWorld->addRigidBody(body);

	btRigidBody* dialogBody = btRigidBody::upcast(dialog->getCollisionObject());
	btAssert(dialogBody);



	btTransform frameInA;
	frameInA.setIdentity();
	btVector3 offset(btVector3(+dialog->getDialogWidth()/2-32,-dialog->getDialogHeight()/2+dialog->getNumControls()*20+36,0.2));
	frameInA.setOrigin(offset);


	btTransform frameInB;
	frameInB.setIdentity();
	//frameInB.setOrigin(-offset/2);
	bool useFrameA = true;

	btGeneric6DofConstraint* constraint = new btGeneric6DofConstraint(*dialogBody,*body,frameInA,frameInB,useFrameA);
	m_dynamicsWorld->addConstraint(constraint,true);


	GL_ToggleControl* toggle = new GL_ToggleControl(toggleText, body,dialog);
	body->setUserPointer(toggle);
	dialog->addControl(toggle);
	return toggle;
}

     */
}