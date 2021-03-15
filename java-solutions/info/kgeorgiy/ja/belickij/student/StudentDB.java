package info.kgeorgiy.ja.belickij.student;

import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.GroupName;
import info.kgeorgiy.java.advanced.student.GroupQuery;
import info.kgeorgiy.java.advanced.student.Student;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements GroupQuery {

    private final Comparator<Student> studentComparator =
            Comparator.comparing(Student::getLastName, Comparator.reverseOrder())
                    .thenComparing(Student::getFirstName, Comparator.reverseOrder())
                    .thenComparing(Student::getId);

    private final Comparator<Group> groupComparator = Comparator.comparing(Group::getName);

    private Stream<Group> toStreamGroup(
            final Stream<Student> stream,
            final Function<Map.Entry<GroupName, List<Student>>, Group> func
    ) {
        return stream.collect(Collectors.groupingBy(Student::getGroup))
                .entrySet()
                .stream()
                .map(func);
    }

    private List<Group> streamToSortedList(
            final Stream<Student> stream,
            final Comparator<Student> studentCmp,
            final Comparator<Group> groupCmp
    ) {
        return toStreamGroup(stream, entry -> new Group(
                entry.getKey(),
                sortStream(entry.getValue().stream(), studentCmp)))
                .sorted(groupCmp)
                .collect(Collectors.toList());
    }

    private GroupName streamToGroupName(
            final Stream<Student> stream,
            final Comparator<Group> groupCmp
    ) {
        return toStreamGroup(stream, element -> new Group(element.getKey(), element.getValue()))
                .max(groupCmp)
                .map(Group::getName)
                .orElse(null);
    }


    private Stream<Student> filterStream(final Stream<Student> stream, final Predicate<Student> predicate) {
        return stream.filter(predicate);
    }

    private List<Student> sortStream(final Stream<Student> stream, final Comparator<Student> comparator) {
        return stream.sorted(comparator).collect(Collectors.toList());
    }

    private List<Student> streamToFilteredList(final Stream<Student> stream, final Predicate<Student> predicate) {
        return sortStream(filterStream(stream, predicate), studentComparator);
    }

    private List<String> mapStream(final Stream<Student> stream, final Function<Student, String> func) {
        return stream.map(func)
                .collect(Collectors.toList());
    }

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return streamToSortedList(
                students.stream(),
                studentComparator,
                groupComparator);
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return streamToSortedList(
                students.stream(),
                Comparator.comparing(Student::getId),
                Comparator.comparing(Group::getName));
    }

    @Override
    public GroupName getLargestGroup(Collection<Student> students) {
        return streamToGroupName(
                students.stream(),
                Comparator.comparing((Group group) -> group.getStudents().size())
                        .thenComparing(Group::getName));
    }

    @Override
    public GroupName getLargestGroupFirstName(Collection<Student> students) {
        return streamToGroupName(
                students.stream(),
                Comparator.comparing((Group group) -> getDistinctFirstNames(group.getStudents()).size())
                        .thenComparing(Group::getName, Comparator.reverseOrder()));
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return mapStream(students.stream(), Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return mapStream(students.stream(), Student::getLastName);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return mapStream(students.stream(), s -> s.getFirstName() + " " + s.getLastName());
    }

    @Override
    public List<GroupName> getGroups(List<Student> students) {
        return students.stream()
                .map(Student::getGroup)
                .collect(Collectors.toList());
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return students.stream()
                .map(Student::getFirstName)
                .collect(Collectors.toSet());
    }


    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return students.stream()
                .max(Comparator.comparingInt(Student::getId))
                .map(Student::getFirstName)
                .orElse("");
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortStream(students.stream(), Comparator.comparingInt(Student::getId));
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortStream(students.stream(), studentComparator);
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return streamToFilteredList(students.stream(), student -> student.getFirstName().equals(name));
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return streamToFilteredList(students.stream(), student -> student.getLastName().equals(name));
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return streamToFilteredList(students.stream(), student -> student.getGroup().equals(group));
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return filterStream(students.stream(), student -> student.getGroup().equals(group))
                .collect(Collectors.toMap(Student::getLastName, Student::getFirstName, BinaryOperator.minBy(String::compareTo)));
    }
}