import java.lang.reflect.Field;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.TreeMap;
import java.util.ArrayList;

public class StudentCourseRegTester {

  private static void checkType(Field f,Class type) throws Exception {
    if(f.getType() != type) {
      throw new Error(
        String.format("The field '%s' of class '%s' should be of type '%s'",f.getName(),f.getDeclaringClass().getName(),type.getName()));
    }
  }

  private static void checkFinal(Field f) {
    if(!Modifier.isFinal(f.getModifiers())) {
      throw new Error(
        String.format("The field '%s' of class '%s' should be final",f.getName(),f.getDeclaringClass().getName()));
    }
  }

  /**
   * Call this method to do your testing.
   */
  public static void testRegistrationsExtended(RegistrationExtendedI src) throws Exception {
    testRegistrations_(src);

    StudentI s  = src.getStudent(123);
    StudentI s2 = src.getStudent(9);
    CourseI  c  = src.getCourse("uw1");

    src.addRegistration(7,"uw1");
    src.addRegistration(13,"uw1");
    src.addRegistration(9,"uw1");
    //src.registerOrWait(666,"ee3"); // already full courses through testRegistrations()
    checkIntegrity(src);
    if(src.checkRegistration(9,"uw1")==null)
      throw new Error(
        String.format("Waiting list didn't work. Student %s didn't get into course %s",s2.getName(),c.getName()));

    src.registerOrWait(123,"uw1");
    checkIntegrity(src);
    if(src.checkRegistration(123,"uw1")!=null)
      throw new Error(
        String.format("Waiting list didn't work. Student %s got into course %s",s.getName(),c.getName()));
    try {
      src.registerOrWait(123,"uw1");
      throw new Error("Student added to waiting list multiple times or Exception not thrown at try.");
    } catch(RegException rex) {
      if(!"AlreadyOnWaitingList".equals(rex.getMessage())) {
        throw new Error("Error message should be 'AlreadyOnWaitingList'");
      }
    }

    src.registerOrWait(4004,"uw1");
    checkIntegrity(src);

    src.unregister(9,"uw1");
    checkIntegrity(src);
    if(src.checkRegistration(123,"uw1")==null)
      throw new Error(
        String.format("Waiting list didn't work. Student %s didn't get into course %s",s.getName(),c.getName()));
    src.unregister(123,"uw1");
    checkIntegrity(src);
    if(src.checkRegistration(123,"uw1")!=null)
      throw new Error(
        String.format("Waiting list did not work: Student %s who was previously on waiting list was either not removed from course %s upon request, or got back into it after unregistering by still being on the waiting list.",s.getName(),c.getName()));
    src.registerOrWait(9,"uw1"); // course is full
    src.registerOrWait(123,"uw1"); // 123 goes into waiting list, but 4004 is first now
    // Not sure if the following should or should not work
    src.unregister(123,"uw1"); // remove 123 from waiting list
    src.registerOrWait(123,"uw1"); // add 123 again
    if(src.checkRegistration(123,"uw1")!=null)
      throw new Error(
        String.format("Waiting list didn't work. Student %s got into course %s",s.getName(),c.getName()));
    src.unregister(13,"uw1"); // 4004 should get in
    if(src.checkRegistration(4004,"uw1")==null)
      throw new Error(
        String.format("Order in waiting list does not work, should be FIFO."));
    src.addRegistration(123,"dda7"); // now 123 cannot get into uw1
    src.unregister(4004,"uw1"); // one spot free, but 123 cannot get in
    if(src.checkRegistration(123,"uw1")!=null)
      throw new Error(
        String.format("Waiting list did not work: Student %s got into course %s",s.getName(),c.getName()));
    src.unregister(123,"dda7"); // Now 123 should be able to get into "uw1"
    // get 123 into uw1
    try {
      src.registerOrWait(123,"uw1");
    } catch(RegException rex) {
      if (!"ExistingRegistration".equals(rex.getMessage()))
        throw new Error(
          String.format("Student %s could not get into course %s", s.getName(),c.getName()));
    }
    // At this point 123 is enrolled, but might or might not still be on the waiting
    // list, depending on implementation - which isn't wrong by itself, just not elegant.
    src.unregister(123,"uw1");
    // Regardless of implementation, 123 should now be not in the course (also not through
    // the waiting list on which he might still be)
    if(src.checkRegistration(123,"uw1")!=null)
      throw new Error(
        String.format("Waiting list did not work: Student %s who was previously on waiting list was either not removed from course %s upon request, or got back into it after unregistering by still being on the waiting list.",s.getName(),c.getName()));

    System.out.println("It works!");
  }

  /**
   * Call this method to do your testing.
   */
  public static void testRegistrations(RegistrationI src) throws Exception {
    testRegistrations_(src);
    System.out.println("It works!");
  }
  public static void testRegistrations_(RegistrationI src) throws Exception {
    Class cl = src.getClass();

    Field regs = cl.getDeclaredField("registrations");
    regs.setAccessible(true);
    checkType(regs,TreeMap.class);

    Field courses = cl.getDeclaredField("courses");
    courses.setAccessible(true);
    checkType(courses,TreeMap.class);

    Field students = cl.getDeclaredField("students");
    students.setAccessible(true);
    checkType(students,TreeMap.class);

    src.readStudents();

    // Students
    StudentI s = src.getStudent(10);
    checkHours(s,0);
    checkName(s,"stud0");

    s = src.getStudent(11);
    checkHours(s,0);
    checkName(s,"stud1");

    s = src.getStudent(22);
    checkHours(s,0);
    checkName(s,"stud2");

    // Test constructor and toString
    String testName = "Foo";
    int    testId = 6;
    Constructor scon = s.getClass().getDeclaredConstructor(String.class,Integer.TYPE);
    s = (StudentI)scon.newInstance(testName,testId);
    if(!testName.equals(s.getName()))
      throw new Error("Bad constructor for class "+s.getClass().getName());
    if(testId != s.getId())
      throw new Error("Bad constructor for class "+s.getClass().getName());
    String testToString = "("+testName+", id="+testId+")";
    if(!testToString.equals(s.toString()))
      throw new Error(String.format("Bad toString() method for class %s. toString() was '%s' instead of '%s'",
        s.getClass().getName(),s.toString(),testToString));

    src.readCourses();

    // Courses
    CourseI c = src.getCourse("cs0");
    checkName(c,"cours0");
    checkHours(c,1);
    checkClassSize(c,0);
    checkMaxClassSize(c,2);

    c = src.getCourse("ee2");
    checkName(c,"cours2");
    checkHours(c,2);
    checkClassSize(c,0);
    checkMaxClassSize(c,2);

    c = src.getCourse("cs1");
    checkName(c,"cours1");
    checkHours(c,1);
    checkClassSize(c,0);
    checkMaxClassSize(c,2);

    c = src.getCourse("ee3");
    checkName(c,"cours3");
    checkHours(c,3);
    checkClassSize(c,0);
    checkMaxClassSize(c,3);

    String testCName = "FCourse";
    String testCId = "Fid";
    int testHours = 3;
    int testMaxSize = 5;
    Constructor ccons = c.getClass().getConstructor(String.class,String.class,Integer.TYPE,Integer.TYPE);
    c = (CourseI)ccons.newInstance(testCName,testCId,testHours,testMaxSize);
    if(!testCName.equals(c.getName()))
      throw new Error("Bad constructor: error initializing name for class "+c.getClass().getName());
    if(!testCId.equals(c.getId()))
      throw new Error("Bad constructor: error initializing id for class "+c.getClass().getName());
    if(testHours != c.getCreditHours())
      throw new Error("Bad constructor: error initializing credit hours for class "+c.getClass().getName());
    if(testMaxSize != c.getMaxClassSize())
      throw new Error("Bad constructor: error initializing max class size for class "+c.getClass().getName());
    String testCToString = String.format("(%s, id=%s, credit: %dh, maxSize: %d)",testCName,testCId,testHours,testMaxSize);
    if(!testCToString.equals(c.toString()))
      throw new Error(String.format("Bad toString() method for class %s. toString() was '%s' instead of '%s'",
        c.getClass().getName(),c.toString(),testCToString));

    try {
      src.addRegistration(100,"cs0");
      throw new Error("No exception thrown when non-existant student is added");
    } catch(RegException re) {
    }
    try {
      src.addRegistration(10,"cx0");
      throw new Error("No exception thrown when non-existant course is added");
    } catch(RegException re) {
    }
    addRegistration(src,10,"cs0");
    try {
      src.addRegistration(10,"cs0");
      throw new Error("No exception thrown for duplicate registration");
    } catch(RegException re) {
    }
    addRegistration(src,11,"cs0");
    try {
      src.addRegistration(22,"cs0");
      throw new Error("No exception thrown when course is over-subscribed");
    } catch(RegException re) {
    }
    addRegistration(src,10,"cs1");
    try {
      src.addRegistration(10,"ee3");
      throw new Error("No exception thrown when student is over-subscribed");
    } catch(RegException re) {
    }
    addRegistration(src,666,"dda7");

    TreeMap regsv = (TreeMap)regs.get(src);
    TreeMap coursesv = (TreeMap)courses.get(src);
    TreeMap studentsv = (TreeMap)students.get(src);
    if(studentsv.size() != 11)
      throw new Error("wrong number of students, should be 11 got "+studentsv.size());
    if(coursesv.size() != 7)
      throw new Error("wrong number of courses, should be 7 got "+coursesv.size());
    if(regsv.size() != 4)
      throw new Error("wrong number of registrations, should be 4 got "+regsv.size());

    s = src.getStudent(10);
    checkHours(s,2);
    s = src.getStudent(11);
    checkHours(s,1);
    s = src.getStudent(22);
    checkHours(s,0);

    c = src.getCourse("cs0");
    checkClassSize(c,2);
    c = src.getCourse("ee2");
    checkClassSize(c,0);
    c = src.getCourse("cs1");
    checkClassSize(c,1);
    c = src.getCourse("ee3");
    checkClassSize(c,0);

    //TreeMap regsv = (TreeMap)regs.get(src);
    for(Object rkey : regsv.keySet()) {
      System.out.print("{");
      boolean first = true;
      for(Field f : rkey.getClass().getDeclaredFields()) {
        if(first) {
          first = false;
        } else {
          System.out.print(",");
        }
        f.setAccessible(true);
        System.out.print(f.getName());
        System.out.print("="+f.get(rkey));
      }
      System.out.println("}");
    }
    checkIntegrity(src);
  }

  private static void checkName(StudentI s,String expectedName) {
    String name = s.getName();
    if(!expectedName.equals(name))
      throw new Error(String.format("Student with id=%d has name=%s, expected name=%s",
        s.getId(),name,expectedName));
  }
  private static void checkName(CourseI c,String expectedName) {
    String name = c.getName();
    if(!expectedName.equals(name))
      throw new Error(String.format("Course with id=%s has name=%s, expected name=%s",
        c.getId(),name,expectedName));
  }
  private static void checkHours(CourseI c,int expected) {
    int hrs = c.getCreditHours();
    if(hrs != expected)
      throw new Error(String.format("Total credit hours for course %s was %d, expected %d",
        c.getId(),hrs,expected));
  }
  private static void checkHours(StudentI s,int expected) {
    int hrs = s.getTotalRegisteredHours();
    if(hrs != expected)
      throw new Error(String.format("Total registered hours for student %d was %d, expected %d",
        s.getId(),hrs,expected));
  }
  private static void checkClassSize(CourseI c,int expected) {
    int sz = c.getClassSize();
    if(sz != expected)
      throw new Error(String.format("Class size for course %s was %d, expected %d",
        c.getId(),sz,expected));
  }
  private static void checkMaxClassSize(CourseI c,int expected) {
    int sz = c.getMaxClassSize();
    if(sz != expected)
      throw new Error(String.format("Max class size for course %s was %d, expected %d",
        c.getId(),sz,expected));
  }
  private static void addRegistration(RegistrationI src,int studentId,String courseId) throws Exception {
    src.addRegistration(studentId,courseId);
    StudentI student = src.getStudent(studentId);
    Field f = student.getClass().getDeclaredField("registeredCourseIds");
    ArrayList al = (ArrayList)f.get(student);
    boolean found = false;
    for(Object o : al) {
      if(courseId.equals(o)) {
        found = true;
        break;
      }
    }
    if(!found) throw new Error("course '"+courseId+"' not found in registeredCourseIds of student '"+student.getName()+"'");
  }

  /**
   * Check total consistency of student and course with the registered keys.
   */
  private static void checkIntegrity(RegistrationI r) throws Exception {
    Class cl = r.getClass();
    Field regs = cl.getDeclaredField("registrations");
    regs.setAccessible(true);
    TreeMap tm = (TreeMap)regs.get(r);
    Field studentIdF = null;
    Field courseIdF = null;
    TreeMap<Integer,TreeMap<String,Boolean>> registeredCourseIds = new TreeMap<>();
    TreeMap<String,Integer> registrations = new TreeMap<>();
    for(Object o : tm.keySet()) {
      if(studentIdF == null) {
        studentIdF = o.getClass().getDeclaredField("studentId");
        studentIdF.setAccessible(true);
      }
      if(courseIdF == null) {
        courseIdF = o.getClass().getDeclaredField("courseId");
        courseIdF.setAccessible(true);
      }
      Integer studentId = (Integer)studentIdF.get(o);
      String  courseId  = (String)courseIdF.get(o);
      //
      TreeMap<String,Boolean> regC = registeredCourseIds.get(studentId);
      if(regC == null)
        registeredCourseIds.put(studentId,regC = new TreeMap<>());
      if(regC.containsKey(courseId))
        throw new Error(
          String.format("duplicate key studentId=%d,courseId=%s",studentId,courseId));
      regC.put(courseId,false);
      //
      if(registrations.containsKey(courseId)) {
        registrations.put(courseId,registrations.get(courseId)+1);
      } else {
        registrations.put(courseId,1);
      }
    }
    for(String courseId : registrations.keySet()) {
      CourseI c = r.getCourse(courseId);
      int cs = c.getClassSize();
      int nr = registrations.get(courseId);
      if(cs != nr) {
        throw new Error(
          String.format("%s.getClassSize()=%d, but there are %d registrations",c.getName(),cs,nr));
      }
      if(nr > c.getMaxClassSize()) {
        throw new Error(
          String.format("Max class size exceeded for class %s",c.getName()));
      }
    }
    Field registeredCourseIdsF = null;
    for(Integer studentId : registeredCourseIds.keySet()) {
      TreeMap<String,Integer> counts = new TreeMap<>();
      TreeMap<String,Boolean> regCourses = registeredCourseIds.get(studentId);
      StudentI stu = r.getStudent(studentId);
      registeredCourseIdsF = stu.getClass().getDeclaredField("registeredCourseIds");
      registeredCourseIdsF.setAccessible(true);
      ArrayList al = (ArrayList)registeredCourseIdsF.get(stu);
      for(Object oc : al) {
        String sc = (String)oc;
        if(regCourses.containsKey(sc)) {
          regCourses.put(sc,true);
        } else {
          throw new Error(
            String.format("Student %s has course %s in registeredCourseIds, but there's no registration key for it.",stu.getName(),sc));
        }
      }
      int hours = 0;
      for(String sc : regCourses.keySet()) {
        if(!regCourses.get(sc))
          throw new Error(
            String.format("Student %s does not have course %s in registeredCourseIds, but should.",stu.getName(),sc));
        CourseI c = r.getCourse(sc);
        hours += c.getCreditHours();
      }
      if(hours != stu.getTotalRegisteredHours())
        throw new Error(
          String.format("Student %s has totalRegisteredHours()==%d, but sum of registrations==%d",
            stu.getName(),stu.getTotalRegisteredHours(),hours));
    }

    //
    Field studentsf = r.getClass().getDeclaredField("students");
    studentsf.setAccessible(true);
    TreeMap students = (TreeMap)studentsf.get(r);
    for(Object ostudentId : students.keySet()) {
      Integer studentId = (Integer)ostudentId;
      if(!registeredCourseIds.containsKey(studentId)) {
        StudentI s = r.getStudent(studentId);
        if(s.getTotalRegisteredHours() != 0) {
          throw new Error(
            String.format("Student %s is not registered for any courses but shows %d total registered hours",s.getName(),s.getTotalRegisteredHours()));
        }
        if(registeredCourseIdsF == null) {
          registeredCourseIdsF = s.getClass().getDeclaredField("registeredCourseIds");
          registeredCourseIdsF.setAccessible(true);
        }
        ArrayList al = (ArrayList)registeredCourseIdsF.get(s);
        if(al.size() != 0) {
          throw new Error(
            String.format("Student %s is not registered for any courses but has entries in registeredCourseIds",s.getName()));
        }
      }
    }

    //
    Field coursesf = r.getClass().getDeclaredField("courses");
    coursesf.setAccessible(true);
    TreeMap courses = (TreeMap)coursesf.get(r);
    for(Object ocourseId : registrations.keySet()) {
      String courseId = (String)ocourseId;
      if(!registrations.containsKey(courseId)) {
        CourseI c = r.getCourse(courseId);
        if(c.getClassSize() != 0) {
          throw new Error(
            String.format("Course %s is not registered for any courses but shows %d total registered hours",c.getName(),c.getClassSize()));
        }
      }
    }
  }