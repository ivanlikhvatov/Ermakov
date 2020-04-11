package ermarov.ermakovHomeWorks.dependencyInjection;

import java.lang.reflect.*;
import java.util.*;

public class Container {
    private Map<String, Object> objects = new HashMap<>();
    private Map<String, Method> methodsWithoutParams = new HashMap<>();
    private Map<String, MethodWithParams> methodsWithParams = new HashMap<>();
    private List<Class> configs = new ArrayList<>();

    public Container(Class... cl) throws Throwable {
        this.configs.addAll(Arrays.asList(cl));
        this.refresh();
    }

    public void refresh() throws Throwable{
        objects.clear();
        sortConfigs();
        createBeans();
    }

    private void sortConfigs(){
        ListIterator<Class> classIterator = configs.listIterator();

        while (classIterator.hasNext()){
            boolean isCoupleSorted = false;

            Class thisClass = classIterator.next();
            Class nextClass = classIterator.next();

            Method[] methodsThisClass = thisClass.getDeclaredMethods();
            Method[] methodsNextClass = nextClass.getDeclaredMethods();

            for (Method methodThisClass: methodsThisClass) {

                if (isCoupleSorted){
                    break;
                }

                for (Method methodNextClass: methodsNextClass) {

                    if (isCoupleSorted){
                        break;
                    }

                    Object[] thisMethodParameterTypes = methodThisClass.getParameterTypes();
                    Type[] thisMethodGenericParameterTypes = methodThisClass.getGenericParameterTypes();
                    List<Class<?>> nextMethodReturnTypes = Arrays.asList(methodNextClass.getReturnType());

                    for (Object parameterType: thisMethodParameterTypes) {

                        if (nextMethodReturnTypes.contains(parameterType)){

                            configs.remove(thisClass);
                            configs.add(thisClass);

                            isCoupleSorted = true;
                            break;

                        }
                    }

                    if (isCoupleSorted){
                        break;
                    }

                    for (Type genericType: thisMethodGenericParameterTypes) {

                        if (!genericType.getClass().toString().equals("class sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl")){
                            continue;
                        }

                        if (nextMethodReturnTypes.contains(((ParameterizedType)genericType).getActualTypeArguments()[0])){

                            configs.remove(thisClass);
                            configs.add(thisClass);

                            isCoupleSorted = true;
                            break;
                        }

                    }

                }
            }

        }
    }

    private void createBeans() throws Throwable {
        for (Class<?> cl: configs) {

            Method[] methods = cl.getDeclaredMethods();

            for (Method method: methods) {

                if (method.isAnnotationPresent(Bean.class)){
                    String txt = method.getAnnotation(Bean.class).value();
                    txt = txt.isEmpty() ? method.getName() : txt;

                    if (objects.containsKey(txt)){
                        throw new IllegalArgumentException("duplicate bean" + txt);
                    }

                    if (method.getParameterTypes().length == 0){

                        if (method.isAnnotationPresent(Prototype.class)){
                            this.objects.put(txt, null);
                            this.methodsWithoutParams.put(txt, method);
                        } else {
                            objects.put(txt, method.invoke(cl.newInstance()));
                        }

                    } else {

                        List<Object> sSysMethodParams = Arrays.asList(method.getParameterTypes());

                        //при желании можно расширить количество Коллекций, которые может принимать метод конфига
                        //(в данном случае допускается что он может принимать либо объект либо лист объетов)
                        List<Object> paramList = new ArrayList<>();
                        List<Object> params = new ArrayList<>();

                        for (Object needTypeParam: sSysMethodParams) {

                            for (Object needParamForMethod : this.objects.values()) {

                                if (needParamForMethod == null){
                                    continue;
                                }

                                if (needTypeParam.equals(needParamForMethod.getClass())){
                                    params.add(needParamForMethod);
                                }

                                if (needTypeParam.toString().equals("interface java.util.List")
                                        && ((ParameterizedType) method.getGenericParameterTypes()[sSysMethodParams.indexOf(needTypeParam)]).getActualTypeArguments()[0]
                                        .equals(needParamForMethod.getClass())){

                                    paramList.add(needParamForMethod);

                                }

                                params.remove(paramList);
                                params.add(paramList);

                            }

                            for (Method needMethodsForMethod: this.methodsWithoutParams.values()){

                                if (needMethodsForMethod.getReturnType().equals(needTypeParam)){
                                    params.add(needMethodsForMethod.invoke(needMethodsForMethod.getDeclaringClass().newInstance()));
                                }

                                if (needTypeParam.toString().equals("interface java.util.List")
                                        && ((ParameterizedType) method.getGenericParameterTypes()[sSysMethodParams.indexOf(needTypeParam)]).getActualTypeArguments()[0]
                                        .equals(needMethodsForMethod.getReturnType())){

                                    paramList.add(needMethodsForMethod.invoke(needMethodsForMethod.getDeclaringClass().newInstance()));

                                }

                            }

                        }

                        if (method.isAnnotationPresent(Prototype.class)){

                            this.objects.put(txt, null);
                            MethodWithParams methodWithParams = new MethodWithParams(params, method);
                            this.methodsWithParams.put(txt, methodWithParams);

                        } else if (!params.isEmpty()){

                            objects.put(txt, method.invoke(cl.newInstance(), params.toArray()));

                        } else {

                            throw new IllegalArgumentException("For something causes " + cl + " cannot be create, check your config fails");

                        }

                    }

                }
            }

        }
    }

    public Object getBean(String name) throws Throwable {
        Object object = objects.get(name);

        if (object != null){
            return object;
        }

        if (methodsWithParams.get(name) != null) {
            return methodsWithParams.get(name).method
                    .invoke(methodsWithParams.get(name).method.getDeclaringClass().newInstance(), methodsWithParams.get(name).params.toArray());
        }

        return methodsWithoutParams.get(name).invoke(methodsWithoutParams.get(name).getDeclaringClass().newInstance());
    }

    public<T> T getBean(String name, Class<T> cl) throws Throwable{
        return (T) getBean(name);
    }

    public List<Object> listObject(){
        return new ArrayList<>(objects.values());
    }

    static class MethodWithParams{
        private List<Object> params;
        private Method method;

        public MethodWithParams(List<Object> params, Method method) {
            this.params = params;
            this.method = method;
        }

    }

}
